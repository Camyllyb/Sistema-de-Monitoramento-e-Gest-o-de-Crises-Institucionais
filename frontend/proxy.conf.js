// Em docker-compose, o frontend fala com o backend pelo nome do serviço
// ("backend"), não por "localhost" (containers têm redes separadas).
// Fora do Docker (ng serve direto na máquina), backend:8080 não existe,
// então cai no fallback localhost:8080.
const target = process.env['BACKEND_PROXY_TARGET'] || 'http://localhost:8080';

module.exports = {
  '/api': {
    target,
    secure: false,
    changeOrigin: true,
    logLevel: 'debug',
  },
};
