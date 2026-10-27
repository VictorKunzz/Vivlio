import axios from 'axios';
import { irParaLogin } from './navegacao';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('vivlio_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const rotaDeAuth = erro.config?.url?.startsWith('/api/auth');
    if (erro.response?.status === 401 && !rotaDeAuth) {
      localStorage.removeItem('vivlio_token');
      irParaLogin();
    }
    return Promise.reject(erro);
  }
);

export default api;
