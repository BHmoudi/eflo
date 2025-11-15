/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/workflow-service/api/v1/:path*',
        destination: 'http://localhost:8091/api/v1/:path*',
      },
      {
        source: '/api/workflow/:path*',
        destination: 'http://localhost:8091/api/v1/:path*',
      },
      {
        source: '/api/users/:path*',
        destination: 'http://localhost:8084/api/v1/:path*',
      },
    ];
  },
};

module.exports = nextConfig;
