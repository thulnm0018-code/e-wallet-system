import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export function Input({ label, error, className = '', type, ...props }: InputProps) {
  const [showPassword, setShowPassword] = useState(false);

  const isPassword = type === 'password';
  const inputType = isPassword ? (showPassword ? 'text' : 'password') : type;

  return (
    <div className="flex flex-col gap-2 w-full relative">
      {label && (
        <label className="uppercase tracking-wide text-[13px] text-charcoal-black/70">
          {label}
        </label>
      )}
      <div className="relative flex items-center w-full">
        <input
          type={inputType}
          className={`
            bg-transparent
            border-0 border-b
            w-full
            ${error ? 'border-[#8B6B6B]' : 'border-grid-line'}
            focus:border-charcoal-black
            focus:outline-none
            px-0 py-3
            pr-10
            text-charcoal-black
            placeholder:text-medium-concrete
            transition-colors duration-100
            ${className}
          `}
          {...props}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-0 p-2 text-charcoal-black/45 hover:text-charcoal-black transition-colors focus:outline-none cursor-pointer"
            tabIndex={-1}
          >
            {showPassword ? (
              <EyeOff className="w-5 h-5" strokeWidth={1.5} />
            ) : (
              <Eye className="w-5 h-5" strokeWidth={1.5} />
            )}
          </button>
        )}
      </div>
      {error && (
        <span className="text-[13px] text-[#8B6B6B] tracking-wide">
          {error}
        </span>
      )}
    </div>
  );
}
