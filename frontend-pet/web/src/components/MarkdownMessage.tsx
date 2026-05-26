import ReactMarkdown from "react-markdown";
import remarkBreaks from "remark-breaks";
import remarkGfm from "remark-gfm";

type Props = {
  /** 流式或完整 Markdown 正文（SSE 增量拼接后由父组件传入） */
  content: string;
  /** AI 气泡为浅色背景；用户气泡为橙色底，需单独配色 */
  variant: "ai" | "user";
};

/**
 * 使用 react-markdown + GFM（表格、任务列表、删除线等）+ remark-breaks（单换行转 &lt;br&gt;，更接近对话排版）。
 * 流式场景下每次 delta 会整段重渲染，属常见做法；极长文本可考虑节流（另议）。
 */
export function MarkdownMessage({ content, variant }: Props) {
  return (
    <div className={`markdown-body markdown-body--${variant}`}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm, remarkBreaks]}
        components={{
          a: ({ href, children, ...props }) => (
            <a href={href ?? "#"} target="_blank" rel="noopener noreferrer" {...props}>
              {children}
            </a>
          ),
          code: ({ className, children, node: _node, ...props }) => {
            const inline = !className;
            return inline ? (
              <code className="md-inline-code" {...props}>
                {children}
              </code>
            ) : (
              <code className={className} {...props}>
                {children}
              </code>
            );
          },
          pre: ({ children }) => <pre className="md-pre">{children}</pre>,
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}
