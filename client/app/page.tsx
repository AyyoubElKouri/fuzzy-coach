import { Suspense } from "react";

const API_URL = process.env.API_URL ?? "http://localhost:8080";

async function Hello() {
  const res = await fetch(`${API_URL}/hello`, { cache: "no-store" });

  if (!res.ok) {
    return (
      <p className="text-red-600">Backend error: {res.status} {res.statusText}</p>
    );
  }

  const message = await res.text();

  return <p className="text-lg">{message}</p>;
}

export default function Home() {
  return (
    <main className="flex flex-1 flex-col items-center justify-center gap-4 p-8">
      <h1 className="text-2xl font-semibold">Fuzzy Coach</h1>
      <Suspense fallback={<p className="text-zinc-500">Loading…</p>}>
        <Hello />
      </Suspense>
    </main>
  );
}
