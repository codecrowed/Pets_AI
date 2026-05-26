---
name: animal-island-ui
description: Generate correct React code using the animal-island-ui component library (17 components). Use when writing, reviewing, or refactoring frontend code in the Pets_AI project that involves UI components like Button, Input, Modal, Card, Select, Tabs, Checkbox, Switch, Collapse, Icon, Typewriter, CodeBlock, Cursor, Time, Phone, Footer, or Divider.
disable-model-invocation: false
---

# animal-island-ui Component Library

> Canonical reference for generating code with `animal-island-ui` in this project.
> Full API details: `frontend-pet/AI_USAGE.md`

## Setup (once per app entry)

```ts
import 'animal-island-ui/style';  // MUST import BEFORE any component usage — only once at entry
```

All components are named exports from the package root:

```ts
import { Button, Input, Switch, Modal, Card, ... } from 'animal-island-ui';
```

## Component Quick Reference

| Component | Required Props | Key Constraints |
|-----------|---------------|-----------------|
| **Button** | — | `type`: primary \| default \| dashed \| text \| link. NOT secondary/outline. `size`: small \| middle \| large. Use `ghost` prop (not type) for ghost. |
| **Input** | — | `size`: small \| middle \| large. Supports controlled & uncontrolled. |
| **Switch** | — | `size`: small \| **default** (NOT middle/large — diverges from others). |
| **Modal** | `open` | Always pair with `onClose`. `typewriter` defaults true. `footer={null}` hides footer. |
| **Card** | — | `color` must be one of 13 `CardColor` values (no hex). `type`: default \| title \| dashed. |
| **Collapse** | `question`, `answer` | Both required. Pure CSS transition, SSR-safe. |
| **Cursor** | — | Wraps region for game-style cursor. Do NOT nest multiple. |
| **Time** | — | Self-contained HUD widget, no configurable props. |
| **Phone** | — | Decorative 527×788px widget, not configurable beyond className. |
| **Footer** | — | `type`: sea \| tree (default tree). |
| **Divider** | — | `type`: line-brown \| line-teal \| line-white \| line-yellow \| wave-yellow. |
| **Typewriter** | — | Emits NO wrapper element. Style children directly. `speed` in ms/char. |
| **Tabs** | `items` | Each item: `{ key, label, children }`. Supports controlled (`activeKey`+`onChange`) and uncontrolled (`defaultActiveKey`). |
| **Icon** | `name` | Must be one of 10 `IconName` values. Use `size` prop, do NOT wrap in sized div. |
| **Select** | `options`, `value`, `onChange` | **Controlled only.** No `defaultValue`. No `className`/`style`. |
| **Checkbox** | `options` | `size`: small \| middle \| large. Values can be `string \| number`. No indeterminate. |
| **CodeBlock** | `code` | JSX/TS only — no `language` prop. |

## Valid CardColor Values

`default` · `app-pink` · `purple` · `app-blue` · `app-yellow` · `app-orange` · `app-teal` · `app-green` · `app-red` · `lime-green` · `yellow-green` · `brown` · `warm-peach-pink`

## Valid IconName Values

`icon-miles` · `icon-camera` · `icon-chat` · `icon-critterpedia` · `icon-design` · `icon-diy` · `icon-helicopter` · `icon-map` · `icon-shopping` · `icon-variant`

## Hard Rules

1. `import 'animal-island-ui/style'` only once at app entry — never per component file.
2. **Never invent props.** Only use props listed in `AI_USAGE.md`. No `variant`, `shape`, `rounded`, `theme`, `color="primary"`, etc.
3. `Modal.open` is required; always provide `onClose`.
4. `Collapse.question` and `Collapse.answer` are both required.
5. Button `type` is `primary | default | dashed | text | link` — NOT `secondary`, `outline`, `ghost`.
6. Switch `size` is `small | default` — NOT `middle | large`.
7. Card `color` must be one of the 13 `CardColor` values — no hex codes.
8. Decorative components (Divider/Footer/Phone/Time/Cursor) accept no style props beyond `className` and `type` where listed.
9. Typewriter emits no wrapper DOM node — style its children instead.
10. Icon `name` must be one of 10 `IconName` values — no arbitrary strings/URLs.
11. Select is controlled-only: `options`, `value`, `onChange` all required.
12. Checkbox `options` is required; values can be `string | number`.
13. CodeBlock only highlights JSX/TS — no `language` prop.
14. Only import from package root (`animal-island-ui`) or `animal-island-ui/style`. No deep paths.
15. Types must be imported from package root.
16. If passing `checked`/`value` (controlled), must also pass `onChange`.
17. Design tokens are NOT exposed as CSS custom properties.
18. Never force sharp corners (`borderRadius: 0`) on interactive elements.
19. Never override the 3D bottom shadow on Button/Input/Switch.

## Common Patterns

### Form row

```tsx
<Card>
  <Input size="large" type="email" allowClear status={invalid ? 'error' : undefined} />
  <Switch checkedChildren="Subscribe" unCheckedChildren="Off" />
  <Button type="primary" htmlType="submit" block>Submit</Button>
</Card>
```

### Confirm dialog

```tsx
<Modal open={open} title="Delete?" onClose={close} footer={
  <>
    <Button onClick={close}>Cancel</Button>
    <Button type="primary" danger onClick={() => { remove(); close(); }}>Delete</Button>
  </>
}>
  This cannot be undone.
</Modal>
```

### FAQ page

```tsx
<Cursor>
  <Divider type="wave-yellow" />
  {faqs.map(f => <Collapse key={f.id} question={f.q} answer={f.a} />)}
  <Footer type="sea" />
</Cursor>
```

## Additional Resources

For complete prop interfaces, type definitions, and more recipes, see [AI_USAGE.md](../../frontend-pet/AI_USAGE.md).
