export function renderSimpleMarkdown(text) {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/gs, m => `<ul>${m}</ul>`)
  html = html.replace(/^(?!<[hul])(.+)$/gm, (m, line) => {
    if (!line.trim() || line.startsWith('<')) return m
    return `<p>${line.trim()}</p>`
  })
  return html
}
