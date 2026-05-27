import { useCallback, useEffect, useRef, useState } from "react";
import { Button, Card, Input } from "animal-island-ui";
import {
  createChat,
  listChats,
  listMessages,
  searchChats,
  sendMessageStream,
} from "../lib/chat-api";
import { uploadFile } from "../lib/file-api";
import type { ChatSummaryDto } from "../lib/chat-types";
import { messageDtoToUi } from "../lib/chat-types";
import { formatChatItemTime, formatMessageClock } from "../lib/format";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { MarkdownMessage } from "./MarkdownMessage";
import { TopBar } from "./TopBar";
import type { Pet } from "../lib/pet-types";

const MAX_CHARS = 2000;

const SUGGESTIONS = [
  "🍽️ 今天应该喂什么？",
  "💊 疫苗还有几天？",
  "🏃 每天运动多久合适？",
  "😟 宠物焦虑怎么缓解？",
  "📷 上传照片分析健康",
];

type DisplayRow =
  | { kind: "text"; id: string; role: "user" | "ai"; content: string; time: string }
  | { kind: "file"; id: string; name: string; time: string };

type Props = {
  onMenuClick: () => void;
  pets: Pet[];
  activePet: Pet | null;
  onSwitchPet: (petId: number) => void;
  onAddPet: () => void;
};

export function AskAiPage({ onMenuClick, pets, activePet, onSwitchPet, onAddPet }: Props) {
  const petName = activePet?.name || "宠物";
  const [groups, setGroups] = useState<{ dateLabel: string; chats: ChatSummaryDto[] }[]>([]);
  const [currentChatId, setCurrentChatId] = useState<string | null>(null);
  const [rows, setRows] = useState<DisplayRow[]>([]);
  const [showWelcome, setShowWelcome] = useState(true);
  const [input, setInput] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const debouncedSearch = useDebouncedValue(searchInput, 350);
  const [pendingFiles, setPendingFiles] = useState<File[]>([]);
  const [streamingText, setStreamingText] = useState<string | null>(null);
  const [loadingList, setLoadingList] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);
  const streamAbortRef = useRef<AbortController | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    return () => streamAbortRef.current?.abort();
  }, []);

  const showToast = useCallback((msg: string, type: "success" | "warning" = "success") => {
    window.dispatchEvent(new CustomEvent("pawpal-toast", { detail: { msg, type } }));
  }, []);

  const refreshList = useCallback(async () => {
    try {
      const q = debouncedSearch.trim();
      const r = q ? await searchChats(q) : await listChats();
      setGroups(r.groups);
    } catch (e) {
      showToast(e instanceof Error ? e.message : "加载会话列表失败", "warning");
    } finally {
      setLoadingList(false);
    }
  }, [debouncedSearch, showToast]);

  useEffect(() => {
    void refreshList();
  }, [refreshList]);

  const loadChat = useCallback(
    async (chatId: string) => {
      setLoadingMessages(true);
      setCurrentChatId(chatId);
      try {
        const res = await listMessages(chatId, { size: 100 });
        const next: DisplayRow[] = [];
        for (const m of res.messages) {
          const ui = messageDtoToUi(m);
          const t = formatMessageClock(m.createdAt);
          next.push({ kind: "text", id: ui.id, role: ui.role, content: ui.content, time: t });
        }
        setRows(next);
        setShowWelcome(next.length === 0);
      } catch (e) {
        showToast(e instanceof Error ? e.message : "加载消息失败", "warning");
      } finally {
        setLoadingMessages(false);
      }
    },
    [showToast]
  );

  const handleSelectChat = (chatId: string) => {
    void loadChat(chatId);
    setHistoryOpen(false);
  };

  const handleNewChat = async () => {
    try {
      const c = await createChat({});
      setCurrentChatId(c.id);
      setRows([]);
      setShowWelcome(true);
      await refreshList();
      setHistoryOpen(false);
      showToast("新对话已开始", "success");
    } catch (e) {
      showToast(e instanceof Error ? e.message : "创建会话失败", "warning");
    }
  };

  const send = async () => {
    const text = input.trim();
    const files = [...pendingFiles];
    if (!text && files.length === 0) return;
    if (text.length > MAX_CHARS) {
      showToast(`内容请控制在 ${MAX_CHARS} 字以内`, "warning");
      return;
    }

    streamAbortRef.current?.abort();
    const ac = new AbortController();
    streamAbortRef.current = ac;

    setSending(true);
    setShowWelcome(false);
    setInput("");
    setPendingFiles([]);
    const now = new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" });

    try {
      let chatId = currentChatId;
      if (!chatId) {
        const c = await createChat({ title: text.slice(0, 40) || "新对话" });
        chatId = c.id;
        setCurrentChatId(chatId);
        await refreshList();
      }

      const attachmentIds: string[] = [];
      for (const f of files) {
        const up = await uploadFile(f, { signal: ac.signal });
        attachmentIds.push(up.id);
      }

      const fileRows: DisplayRow[] = files.map((f, i) => ({
        kind: "file" as const,
        id: `local-file-${Date.now()}-${i}`,
        name: f.name,
        time: now,
      }));
      const userRows: DisplayRow[] = [];
      if (text) {
        userRows.push({
          kind: "text",
          id: `local-user-${Date.now()}`,
          role: "user",
          content: text,
          time: now,
        });
      }
      setRows((prev) => [...prev, ...fileRows, ...userRows]);

      let accumulatedContent = "";
      let savedMessageUid: string | null = null;

      await sendMessageStream(
        chatId,
        text || "（附件）",
        {
          onMessageStart: () => {
            accumulatedContent = "";
            setStreamingText("");
          },
          onDelta: (delta) => {
            accumulatedContent += delta;
            setStreamingText((s) => (s ?? "") + delta);
          },
          onMessageEnd: (data) => {
            savedMessageUid = data.savedMessageUid ?? null;
            setStreamingText(null);
            const aiMessageId = savedMessageUid ?? `ai-${Date.now()}`;
            setRows((prev) => [
              ...prev,
              {
                kind: "text",
                id: aiMessageId,
                role: "ai",
                content: accumulatedContent,
                time: new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" }),
              },
            ]);
          },
          onError: (err) => {
            showToast(err.message, "warning");
            setStreamingText(null);
          },
        },
        attachmentIds.length ? attachmentIds : null,
        { signal: ac.signal }
      );

      await refreshList();
    } catch (e) {
      if (e instanceof Error && e.name === "AbortError") return;
      showToast(e instanceof Error ? e.message : "发送失败", "warning");
    } finally {
      setSending(false);
      setStreamingText(null);
      streamAbortRef.current = null;
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      void send();
    }
  };

  const autoResize = () => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = "auto";
    el.style.height = `${Math.min(el.scrollHeight, 120)}px`;
  };

  useEffect(() => {
    autoResize();
  }, [input]);

  const onPickFiles = (e: React.ChangeEvent<HTMLInputElement>) => {
    const list = e.target.files;
    if (!list?.length) return;
    setPendingFiles((prev) => [...prev, ...Array.from(list)]);
    e.target.value = "";
  };

  const removePending = (idx: number) => {
    setPendingFiles((prev) => prev.filter((_, i) => i !== idx));
  };

  const quickAsk = (t: string) => {
    setInput(t);
    textareaRef.current?.focus();
  };

  const chatQuickAction = (type: "profile" | "case" | "calorie" | "diet" | "avatar" | "more") => {
    switch (type) {
      case "profile":
        showToast("请从左侧菜单进入「宠物档案」添加或编辑信息", "success");
        break;
      case "case":
        quickAsk("请结合我家宠物的档案，帮我做一次健康状况与病例风险方面的分析。");
        break;
      case "calorie":
        quickAsk("请根据档案估算我家宠物每日所需热量，并给出饮食搭配建议。");
        break;
      case "diet":
        showToast("请从左侧菜单进入「饮食记录」", "success");
        break;
      case "avatar":
        showToast("进入「宠物档案」页可设置萌宠头像", "success");
        break;
      case "more":
        showToast("语音通话、深度思考等能力陆续上线", "success");
        break;
    }
  };

  return (
    <>
      <TopBar
        title="问AI助手"
        subtitle="和 PawPal AI 聊聊你的宠物"
        onMenuClick={onMenuClick}
        pets={pets}
        activePet={activePet}
        onSwitchPet={onSwitchPet}
        onAddPet={onAddPet}
      />

      <div className="page-content">
        <Button
          type="default"
          size="small"
          className="topbar-history-btn chat-history-toggle"
          onClick={() => setHistoryOpen((o) => !o)}
          aria-label="对话历史"
        >
          📜 历史
        </Button>
        <div className="page active" id="page-chat">
          <div className="chat-history-overlay" data-open={historyOpen} onClick={() => setHistoryOpen(false)} />
          <div className={`chat-layout${historyOpen ? " history-open" : ""}`}>
            <div className={`chat-history${historyOpen ? " open" : ""}`} id="chatHistory">
              <div className="ch-header">
                <span className="ch-title">对话历史</span>
                <Button
                  type="primary"
                  size="small"
                  onClick={() => void handleNewChat()}
                >
                  ＋ 新对话
                </Button>
              </div>
              <div className="ch-search">
                <Input
                  type="search"
                  placeholder="搜索历史…"
                  prefix={<span aria-hidden>🔍</span>}
                  allowClear
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  onClear={() => setSearchInput("")}
                  autoComplete="off"
                />
              </div>
              <div className="ch-list">
                {loadingList && <div className="ch-empty">加载中…</div>}
                {!loadingList &&
                  groups.flatMap((g) => [
                    <div key={`g-${g.dateLabel}`} className="ch-group-label">
                      {g.dateLabel}
                    </div>,
                    ...g.chats.map((c) => (
                      <button
                        key={c.id}
                        type="button"
                        className={`ch-item${currentChatId === c.id ? " active" : ""}`}
                        onClick={() => handleSelectChat(c.id)}
                      >
                        <span className="ch-item-icon" aria-hidden>
                          🤖
                        </span>
                        <div className="ch-item-body">
                          <div className="ch-item-title">{c.title || "未命名对话"}</div>
                        </div>
                        <span className="ch-item-time">{formatChatItemTime(c.updatedAt)}</span>
                      </button>
                    )),
                  ])}
                {!loadingList && groups.every((g) => g.chats.length === 0) && (
                  <div className="ch-empty">暂无会话</div>
                )}
              </div>
            </div>

            <div className="chat-main">
              <div className="chat-messages" id="chatMessages">
                {showWelcome && rows.length === 0 && streamingText === null && (
                  <div className="chat-welcome" id="chatWelcome">
                    <div className="cw-avatar" aria-hidden>
                      🐾
                    </div>
                    <div className="cw-title">
                      你好！我是 <span>PawPal AI</span>
                    </div>
                    <div className="cw-subtitle">
                      我是 {petName} 的专属 AI 顾问，了解它的健康档案。
                      <br />
                      有任何关于宠物的问题，随时告诉我！
                    </div>
                    <div className="cw-suggestions">
                      {SUGGESTIONS.map((s) => (
                        <Button
                          key={s}
                          type="default"
                          size="small"
                          className="cw-sug-btn"
                          onClick={() => quickAsk(s)}
                        >
                          {s}
                        </Button>
                      ))}
                    </div>
                  </div>
                )}

                {loadingMessages && <div className="ch-empty">加载消息…</div>}

                {rows.map((row) =>
                  row.kind === "file" ? (
                    <div key={row.id} className="msg-row user">
                      <div className="msg-avatar user" aria-hidden>
                        😊
                      </div>
                      <div>
                        <Card className="file-bubble">
                          <div className="file-icon-box" aria-hidden>
                            📄
                          </div>
                          <div>
                            <div className="file-name">{row.name}</div>
                            <div className="file-size">待发送</div>
                          </div>
                        </Card>
                        <div className="msg-time">{row.time}</div>
                      </div>
                    </div>
                  ) : (
                    <div key={row.id} className={`msg-row ${row.role}`}>
                      <div className={`msg-avatar ${row.role}`} aria-hidden>
                        {row.role === "ai" ? "🐾" : "😊"}
                      </div>
                      <div>
                        <div className="msg-bubble">
                          <MarkdownMessage
                            content={row.content}
                            variant={row.role === "ai" ? "ai" : "user"}
                          />
                        </div>
                        <div className="msg-time">{row.time}</div>
                      </div>
                    </div>
                  )
                )}

                {streamingText !== null && (
                  <div className="msg-row ai">
                    <div className="msg-avatar ai" aria-hidden>
                      🐾
                    </div>
                    <div>
                      <div className="msg-bubble">
                        <MarkdownMessage content={streamingText} variant="ai" />
                      </div>
                    </div>
                  </div>
                )}

                {sending && streamingText === null && (
                  <div className="msg-row ai" id="typingRow">
                    <div className="msg-avatar ai" aria-hidden>
                      🐾
                    </div>
                    <div className="msg-bubble" style={{ padding: 0 }}>
                      <div className="typing-indicator" aria-label="AI 正在思考">
                        <div className="typing-dot" />
                        <div className="typing-dot" />
                        <div className="typing-dot" />
                      </div>
                    </div>
                  </div>
                )}
              </div>

              <div className="chat-input-wrap">
                {pendingFiles.length > 0 && (
                  <div className="upload-preview-area">
                    {pendingFiles.map((f, i) => (
                      <div key={`${f.name}-${i}`} className="upload-chip">
                        <span>{f.name}</span>
                        <button type="button" className="upload-chip-remove" onClick={() => removePending(i)}>
                          ✕
                        </button>
                      </div>
                    ))}
                  </div>
                )}
                <div className="chat-input-box">
                  <textarea
                    ref={textareaRef}
                    className="chat-textarea"
                    id="chatInput"
                    rows={1}
                    placeholder="说点什么吧…"
                    autoComplete="off"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onInput={autoResize}
                    onKeyDown={handleKeyDown}
                    disabled={sending}
                  />
                  <div className="chat-tools">
                    <label className="chat-tool-btn" title="上传文件" style={{ cursor: "pointer" }}>
                      📎
                      <input
                        ref={fileRef}
                        type="file"
                        style={{ display: "none" }}
                        accept="image/*,.pdf,.doc,.docx"
                        multiple
                        onChange={onPickFiles}
                      />
                    </label>
                    <button
                      type="button"
                      className="chat-tool-btn"
                      title="语音输入"
                      onClick={() => showToast("语音功能即将上线", "warning")}
                    >
                      🎙️
                    </button>
                    <Button
                      type="primary"
                      size="small"
                      className="send-btn"
                      title="发送"
                      onClick={() => void send()}
                      disabled={sending || (!input.trim() && pendingFiles.length === 0)}
                    >
                      <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor" aria-hidden>
                        <path d="M2 21l21-9L2 3v7l15 2-15 2v7z" />
                      </svg>
                    </Button>
                  </div>
                </div>
                <div className="chat-quick-actions" aria-label="快捷功能">
                  {(
                    [
                      { key: "profile", label: "添加档案" },
                      { key: "case", label: "病例分析" },
                      { key: "calorie", label: "热量分析" },
                      { key: "diet", label: "记录饮食" },
                      { key: "avatar", label: "萌宠头像" },
                      { key: "more", label: "更多" },
                    ] as const
                  ).map(({ key, label }) => (
                    <Button
                      key={key}
                      type="default"
                      size="small"
                      className="chat-quick-chip"
                      onClick={() => chatQuickAction(key)}
                    >
                      {label}
                    </Button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
