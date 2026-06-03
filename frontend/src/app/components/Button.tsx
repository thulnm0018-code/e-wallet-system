interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  children: React.ReactNode;
}

export function Button({ variant = 'primary', children, className = '', ...props }: ButtonProps) {
  const baseStyles = 'px-8 py-4 transition-colors duration-100 border-0 uppercase tracking-wider';

  const variants = {
    primary: 'bg-charcoal-black text-stone-white hover:bg-[#2A2A2A]',
    secondary: 'bg-concrete-gray text-charcoal-black hover:bg-medium-concrete',
    ghost: 'bg-transparent text-charcoal-black hover:bg-concrete-gray border border-grid-line'
  };

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
