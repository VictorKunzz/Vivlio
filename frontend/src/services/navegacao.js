// Navegação fora do React Router, usada pelo interceptor do axios.
// Fica isolada para os testes poderem substituí-la (o jsdom não navega).
export function irParaLogin() {
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}
