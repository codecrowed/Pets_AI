type Props = {
  options: [string, string, string];
  disabled?: boolean;
  onSelect: (text: string) => void;
};

export function AiQuickReplies({ options, disabled, onSelect }: Props) {
  return (
    <div className="msg-quick-replies" aria-label="快捷回复">
      {options.map((text) => (
        <button
          key={text}
          type="button"
          className="msg-quick-reply-chip"
          disabled={disabled}
          onClick={() => onSelect(text)}
        >
          {text}
        </button>
      ))}
    </div>
  );
}
