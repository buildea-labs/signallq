"use client";

import { useEffect } from "react";
import { initTelemetryDeferred } from "@/lib/telemetry";

// `initTelemetryDeferred` já existia em telemetry.ts mas nunca era chamado —
// sem isso, session_start/session_end nunca eram emitidos e o site não
// gerava nenhum dado de visita em analytics_events (platform='web'). Esse
// componente é o único ponto de disparo, montado uma vez no layout raiz.
export function TelemetryInit() {
  useEffect(() => {
    initTelemetryDeferred();
  }, []);

  return null;
}
