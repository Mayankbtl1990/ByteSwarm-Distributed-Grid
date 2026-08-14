import React from 'react';

export default function MessageLog({ messages }) {
  return (
    <div className="message-log">
      <div className="log-header">
        <span> Live Message Log</span>
        <span className="log-count mono">{messages.length} messages</span>
      </div>
      <div className="log-body">
        {messages.length === 0 ? (
          <div className="log-empty">Waiting for messages...</div>
        ) : (
          messages.slice().reverse().map((msg, i) => (
            <div key={i} className="log-entry">
              <span className="log-time mono">
                {new Date(msg.receivedAt).toLocaleTimeString()}
              </span>
              <span className={`log-type type-${msg.type?.toLowerCase()}`}>
                {msg.type}
              </span>
              <span className="log-data mono">
                {JSON.stringify(msg.data || {}).slice(0, 80)}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}