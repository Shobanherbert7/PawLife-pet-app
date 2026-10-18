const API_BASE_URL = 'http://localhost:4000/api';
const WS_BASE_URL = 'ws://localhost:4000';

export async function fetchServerHealth() {
  try {
    const res = await fetch(`${API_BASE_URL}/health`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    return { status: 'offline', error: err.message };
  }
}

export async function fetchServerPets() {
  try {
    const res = await fetch(`${API_BASE_URL}/pets`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.warn('[API] Could not fetch pets from dedicated server:', err.message);
    return null;
  }
}

export async function fetchLatestTelemetry(petId = 1) {
  try {
    const res = await fetch(`${API_BASE_URL}/telemetry/latest/${petId}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.warn('[API] Could not fetch telemetry:', err.message);
    return null;
  }
}

export async function postCollarTelemetry(data) {
  try {
    const res = await fetch(`${API_BASE_URL}/telemetry`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return await res.json();
  } catch (err) {
    console.warn('[API] Failed to post telemetry:', err.message);
    return null;
  }
}

export async function fetchFamilyTeam(petId = 1) {
  try {
    const res = await fetch(`${API_BASE_URL}/family/${petId}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.warn('[API] Could not fetch family team:', err.message);
    return null;
  }
}

export async function fetchServerWalks(petId = 1) {
  try {
    const res = await fetch(`${API_BASE_URL}/walks/${petId}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.warn('[API] Could not fetch walks from server:', err.message);
    return [];
  }
}

export async function fetchServerFeedings(petId = 1) {
  try {
    const res = await fetch(`${API_BASE_URL}/feeding/${petId}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.warn('[API] Could not fetch feedings from server:', err.message);
    return [];
  }
}

export function subscribeToTelemetryWebSocket(onMessageReceived) {
  try {
    const socket = new WebSocket(WS_BASE_URL);

    socket.onopen = () => {
      console.log('✅ Connected to PawLife Real-time Telemetry WebSocket');
    };

    socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (onMessageReceived) {
          onMessageReceived(data);
        }
      } catch (e) {
        console.error('Error parsing telemetry frame', e);
      }
    };

    socket.onerror = (err) => {
      console.warn('Telemetry WebSocket error:', err);
    };

    return () => {
      if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
        socket.close();
      }
    };
  } catch (e) {
    console.warn('Could not establish WebSocket connection:', e);
    return () => {};
  }
}
