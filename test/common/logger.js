const start = Date.now()

export function step(role, message, detail) {
  const ts = new Date().toISOString().slice(11, 19)
  const roleLabel = role ? `[${role}]` : ''
  console.log(`${ts} ${roleLabel} ${message}${detail ? ` — ${detail}` : ''}`)
}

export function ok(message) {
  console.log(`  ✓ ${message}`)
}

export function fail(message) {
  console.error(`  ✗ ${message}`)
}

export function banner(title) {
  console.log('\n' + '='.repeat(60))
  console.log(title)
  console.log('='.repeat(60))
}

export function elapsed() {
  return ((Date.now() - start) / 1000).toFixed(1) + 's'
}
