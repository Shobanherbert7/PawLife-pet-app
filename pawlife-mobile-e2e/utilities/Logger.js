const { createLogger, format, transports } = require('winston');
const path = require('path');
const fs = require('fs');
require('winston-daily-rotate-file');

const logsDir = path.resolve(__dirname, '../logs');
if (!fs.existsSync(logsDir)) {
  fs.mkdirSync(logsDir, { recursive: true });
}

const customFormat = format.printf(({ timestamp, level, message }) => {
  return `[${timestamp}] [${level.toUpperCase()}]: ${message}`;
});

const logger = createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: format.combine(
    format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
    format.errors({ stack: true }),
    customFormat
  ),
  transports: [
    new transports.Console({
      format: format.combine(
        format.colorize(),
        format.timestamp({ format: 'HH:mm:ss' }),
        format.printf(({ timestamp, level, message }) => `[${timestamp}] ${level}: ${message}`)
      )
    }),
    new transports.DailyRotateFile({
      filename: path.join(logsDir, 'mobile-execution-%DATE%.log'),
      datePattern: 'YYYY-MM-DD',
      maxFiles: '14d',
      level: 'info'
    }),
    new transports.File({
      filename: path.join(logsDir, 'failures.log'),
      level: 'error'
    })
  ]
});

module.exports = logger;
