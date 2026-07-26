import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, ArrowLeft, Check } from 'lucide-react';
import { toast } from 'sonner';
import { fetchNotifications, markAllNotificationsRead, markNotificationRead } from '../services/adminNotificationService';
import type { NotificationItem } from '../services/adminTypes';

function isBackendNotImplemented(error: unknown) {
  const status = (error as any)?.response?.status;
  return status === 404 || status === 501;
}

export function Notifications() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionInProgress, setActionInProgress] = useState<number | null>(null);

  const loadNotifications = async () => {
    setError(null);
    setLoading(true);
    try {
      const list = await fetchNotifications();
      setNotifications(list || []);
    } catch (err) {
      if (isBackendNotImplemented(err)) {
        setError('Backend API not implemented yet');
      } else {
        setError((err as any)?.response?.data?.message || (err as Error).message || 'Unable to load notifications');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadNotifications();
  }, []);

  const handleMarkRead = async (id: number) => {
    setActionInProgress(id);
    try {
      await markNotificationRead(id);
      toast.success('Notification marked read');
      await loadNotifications();
    } catch (err) {
      toast.error((err as any)?.response?.data?.message || (err as Error).message || 'Unable to update notification');
    } finally {
      setActionInProgress(null);
    }
  };

  const handleMarkAllRead = async () => {
    setActionInProgress(-1);
    try {
      const unreadIds = notifications.filter((item) => !item.read).map((item) => item.id);
      if (unreadIds.length > 0) {
        await markAllNotificationsRead(unreadIds);
        toast.success('All notifications marked read');
        await loadNotifications();
      }
    } catch (err) {
      toast.error((err as any)?.response?.data?.message || (err as Error).message || 'Unable to update notifications');
    } finally {
      setActionInProgress(null);
    }
  };

  const formatDate = (value?: string) => {
    if (!value) return 'N/A';
    return new Date(value).toLocaleString();
  };

  return (
    <main className="min-h-screen bg-stone-white text-charcoal-black px-4 py-8 sm:px-8 lg:px-12">
      <div className="mx-auto max-w-[900px] space-y-8">
        <div className="flex flex-col gap-4 border border-grid-line bg-stone-white p-6 sm:p-8 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <div className="text-[11px] uppercase tracking-[0.35em] text-medium-concrete">System</div>
            <h1 className="mt-3 text-[28px] font-black uppercase tracking-[0.03em]">Notifications</h1>
          </div>
          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="inline-flex items-center gap-2 border border-grid-line px-4 py-3 text-[12px] uppercase tracking-[0.25em] text-charcoal-black hover:bg-concrete-gray"
            >
              <ArrowLeft className="h-4 w-4" />
              Back
            </button>
            <button
              type="button"
              disabled={actionInProgress === -1 || notifications.every((item) => item.read)}
              onClick={handleMarkAllRead}
              className="inline-flex items-center gap-2 border border-charcoal-black px-4 py-3 text-[12px] uppercase tracking-[0.25em] text-charcoal-black hover:bg-charcoal-black hover:text-stone-white disabled:cursor-not-allowed disabled:opacity-40"
            >
              <Check className="h-4 w-4" />
              Mark all read
            </button>
          </div>
        </div>

        <section className="border border-grid-line bg-stone-white p-8 space-y-6">
          <div className="flex items-center gap-3 text-[12px] uppercase tracking-[0.25em] text-medium-concrete">
            <Bell className="h-4 w-4" />
            <span>Latest notifications</span>
          </div>

          {loading ? (
            <div className="py-20 text-center text-[13px] uppercase tracking-[0.25em] text-medium-concrete">LOADING NOTIFICATIONS…</div>
          ) : error ? (
            <div className="py-20 text-center text-[13px] uppercase tracking-[0.25em] text-error">{error}</div>
          ) : notifications.length === 0 ? (
            <div className="py-20 text-center text-[13px] uppercase tracking-[0.25em] text-medium-concrete">NO NOTIFICATIONS AVAILABLE</div>
          ) : (
            <div className="space-y-4">
              {notifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`border border-grid-line p-5 ${notification.read ? 'bg-stone-white' : 'bg-concrete-gray/20'}`}
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div className="space-y-2">
                      <div className="text-[12px] uppercase tracking-[0.2em] text-charcoal-black font-bold">{notification.title || 'System notice'}</div>
                      <div className="text-[15px] leading-relaxed text-charcoal-black/80">{notification.content}</div>
                    </div>
                    <div className="flex flex-col items-start gap-2 sm:items-end">
                      <span className="text-[10px] uppercase tracking-[0.25em] text-medium-concrete">{formatDate(notification.createdAt)}</span>
                      {!notification.read && (
                        <span className="px-2 py-1 text-[10px] uppercase tracking-[0.25em] bg-charcoal-black text-stone-white font-bold">UNREAD</span>
                      )}
                    </div>
                  </div>
                  {!notification.read && (
                    <div className="mt-4 flex justify-end">
                      <button
                        type="button"
                        disabled={actionInProgress === notification.id}
                        onClick={() => handleMarkRead(notification.id)}
                        className="px-4 py-3 border border-charcoal-black uppercase tracking-[0.2em] text-[11px] font-bold text-charcoal-black hover:bg-charcoal-black hover:text-stone-white rounded-none transition-colors duration-100"
                      >
                        {actionInProgress === notification.id ? 'UPDATING' : 'Mark read'}
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  );
}
