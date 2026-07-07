/** زخرفة هندسية إسلامية — شمسة SVG مبسطة */
export default function GeomOrnament({
  size = 80,
  color = "var(--color-gold)",
  opacity = 0.12,
  className = "",
}: {
  size?: number;
  color?: string;
  opacity?: number;
  className?: string;
}) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      className={className}
      style={{ opacity }}
      aria-hidden="true"
    >
      {/* outer circle */}
      <circle cx="50" cy="50" r="48" fill="none" stroke={color} strokeWidth="0.8" />
      {/* inner circle */}
      <circle cx="50" cy="50" r="32" fill="none" stroke={color} strokeWidth="0.6" />
      {/* 8-pointed star */}
      {Array.from({ length: 8 }, (_, i) => {
        const angle = (i * 45 * Math.PI) / 180;
        const inner = 18;
        const outer = 46;
        const halfA = (22.5 * Math.PI) / 180;
        const x1 = 50 + outer * Math.cos(angle);
        const y1 = 50 + outer * Math.sin(angle);
        const x2 = 50 + inner * Math.cos(angle + halfA);
        const y2 = 50 + inner * Math.sin(angle + halfA);
        return (
          <line
            key={i}
            x1={x1}
            y1={y1}
            x2={x2}
            y2={y2}
            stroke={color}
            strokeWidth="0.7"
          />
        );
      })}
      {/* spokes */}
      {Array.from({ length: 8 }, (_, i) => {
        const angle = (i * 45 * Math.PI) / 180;
        return (
          <line
            key={`s${i}`}
            x1={50 + 14 * Math.cos(angle)}
            y1={50 + 14 * Math.sin(angle)}
            x2={50 + 46 * Math.cos(angle)}
            y2={50 + 46 * Math.sin(angle)}
            stroke={color}
            strokeWidth="0.5"
          />
        );
      })}
      {/* center dot */}
      <circle cx="50" cy="50" r="3" fill={color} opacity="0.5" />
    </svg>
  );
}
