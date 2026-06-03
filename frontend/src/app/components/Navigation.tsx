import { useNavigate } from 'react-router-dom';
import { Wallet, Bell } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export function Navigation() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  return (
    <nav className="border-b border-grid-line bg-stone-white">
      <div className="max-w-[1440px] mx-auto px-8">
        <div className="flex items-center justify-between h-20 text-[12px] uppercase tracking-[0.15em] text-charcoal-black">
          {/* Left side: Original E-WALLET Logo and Nav links */}
          <div className="flex items-center gap-12">
            <button
              onClick={() => navigate('/dashboard')}
              className="flex items-center gap-3 hover:opacity-80 transition-opacity duration-150 cursor-pointer"
            >
              <div className="w-12 h-12 bg-charcoal-black flex items-center justify-center">
                <Wallet className="w-6 h-6 text-stone-white" strokeWidth={1.5} />
              </div>
              <div className="text-[20px] tracking-[0.05em] font-black text-charcoal-black">
                E-WALLET
              </div>
            </button>

          </div>
          
          {/* Right side: Profile, Wallet ID, Notifications, Logout separated by thin lines */}
          <div className="flex items-center gap-6 h-full font-medium">
            <div className="flex items-center gap-3">
              <span className="text-charcoal-black font-bold">{user?.name ?? 'ANDO TADAO'}</span>
            </div>
            <span className="w-px h-4 bg-grid-line" aria-hidden="true" />
            <span className="text-medium-concrete">ID: WL-8802-9901</span>
            <span className="w-px h-4 bg-grid-line" aria-hidden="true" />

            <button className="hover:text-medium-concrete transition-colors duration-100 cursor-pointer flex items-center gap-2">
              <Bell className="w-5 h-5" />
              <span className="text-[12px]">2</span>
            </button>
          </div>

          {/* Fixed logout button on bottom-right of screen */}
          {user && (
            <button
              onClick={() => { logout(); navigate('/login'); }}
              className="fixed bottom-6 right-6 z-50 bg-charcoal-black text-stone-white px-4 py-3 font-extrabold uppercase tracking-wider text-[12px] rounded-none border border-grid-line hover:bg-concrete-gray transition-colors duration-150"
            >
              Logout
            </button>
          )}
        </div>
      </div>
    </nav>
  );
}


