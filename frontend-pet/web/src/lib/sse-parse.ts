export type SseEvent = { event: string; data: string };

const SSE_GAP = /\r\n\r\n|\n\n/;

export function appendSseBuffer(
  buffer: string,
  chunk: string
): { events: SseEvent[]; rest: string } {
  const full = buffer + chunk;
  const events: SseEvent[] = [];
  let rest = full;
  let m: RegExpExecArray | null;
  while ((m = SSE_GAP.exec(rest))) {
    const sep = m.index;
    const block = rest.slice(0, sep);
    rest = rest.slice(sep + m[0].length);
    const parsed = parseSseBlock(block.replace(/\r\n/g, "\n"));
    if (parsed) events.push(parsed);
    SSE_GAP.lastIndex = 0;
  }
  return { events, rest };
}

function parseSseBlock(block: string): SseEvent | null {
  const lines = block.split("\n").filter((l) => l.length > 0);
  if (lines.length === 0) return null;
  let eventName = "message";
  const dataLines: string[] = [];
  for (const line of lines) {
    if (line.startsWith("event:")) {
      eventName = line.slice(6).trim();
    } else if (line.startsWith("data:")) {
      dataLines.push(line.slice(5).startsWith(" ") ? line.slice(6) : line.slice(5));
    }
  }
  if (dataLines.length === 0) return null;
  return { event: eventName, data: dataLines.join("\n") };
}
