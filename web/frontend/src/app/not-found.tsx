import Link from "next/link";

export default function NotFound() {
  return (
    <div className="space-y-6 py-20 text-center">
      <p className="label">404 · not found</p>
      <h1 className="display-mono text-5xl font-light">$ stat /this/page</h1>
      <p className="mx-auto max-w-md text-sm text-muted">
        We couldn't find this page. The URL may have been mistyped or the
        room/version no longer exists.
      </p>
      <Link href="/" className="pill-primary font-mono uppercase tracking-button text-xs">
        Back to home
      </Link>
    </div>
  );
}
