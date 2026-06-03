interface CardProps {
  children: React.ReactNode;
  className?: string;
}

export function Card({ children, className = '' }: CardProps) {
  return (
    <div className={`bg-concrete-gray border border-grid-line ${className}`}>
      {children}
    </div>
  );
}
