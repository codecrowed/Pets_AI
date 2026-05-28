/** Mock 快捷回复候选（后续可替换为接口返回） */
const QUICK_REPLY_POOL = [
  "能再详细说说吗？",
  "有没有更简单的做法？",
  "还有什么需要注意的？",
  "适合我家宠物的情况吗？",
  "可以举个例子吗？",
  "还有其他建议吗？",
  "大概需要多久见效？",
  "日常护理要注意什么？",
];

function hashSeed(id: string): number {
  let h = 0;
  for (let i = 0; i < id.length; i++) {
    h = (h * 31 + id.charCodeAt(i)) | 0;
  }
  return Math.abs(h);
}

/** 根据消息 id 稳定取出 3 条 mock 快捷回复 */
export function getMockQuickReplies(messageId: string): [string, string, string] {
  const seed = hashSeed(messageId);
  const n = QUICK_REPLY_POOL.length;
  const i0 = seed % n;
  const i1 = (seed + 3) % n;
  const i2 = (seed + 7) % n;
  return [QUICK_REPLY_POOL[i0], QUICK_REPLY_POOL[i1], QUICK_REPLY_POOL[i2]];
}
