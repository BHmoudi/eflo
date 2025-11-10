import React, { useState, useRef, useEffect } from 'react';
import { createPortal } from 'react-dom';

export interface TooltipProps {
  children: React.ReactNode;
  content: React.ReactNode;
  position?: 'top' | 'bottom' | 'left' | 'right';
  trigger?: 'hover' | 'click';
  delay?: number;
  darkMode?: boolean;
  showArrow?: boolean;
  disabled?: boolean;
  className?: string;
}

export const Tooltip: React.FC<TooltipProps> = ({
  children,
  content,
  position = 'top',
  trigger = 'hover',
  delay = 200,
  darkMode = false,
  showArrow = true,
  disabled = false,
  className = '',
}) => {
  const [isVisible, setIsVisible] = useState(false);
  const [coords, setCoords] = useState({ top: 0, left: 0 });
  const triggerRef = useRef<HTMLDivElement>(null);
  const tooltipRef = useRef<HTMLDivElement>(null);
  const timeoutRef = useRef<NodeJS.Timeout>();

  const calculatePosition = () => {
    if (!triggerRef.current || !tooltipRef.current) return;

    const triggerRect = triggerRef.current.getBoundingClientRect();
    const tooltipRect = tooltipRef.current.getBoundingClientRect();
    const spacing = 8;
    const arrowSize = showArrow ? 6 : 0;

    let top = 0;
    let left = 0;

    switch (position) {
      case 'top':
        top = triggerRect.top - tooltipRect.height - spacing - arrowSize;
        left = triggerRect.left + (triggerRect.width - tooltipRect.width) / 2;
        break;
      case 'bottom':
        top = triggerRect.bottom + spacing + arrowSize;
        left = triggerRect.left + (triggerRect.width - tooltipRect.width) / 2;
        break;
      case 'left':
        top = triggerRect.top + (triggerRect.height - tooltipRect.height) / 2;
        left = triggerRect.left - tooltipRect.width - spacing - arrowSize;
        break;
      case 'right':
        top = triggerRect.top + (triggerRect.height - tooltipRect.height) / 2;
        left = triggerRect.right + spacing + arrowSize;
        break;
    }

    // Ensure tooltip stays within viewport
    const padding = 10;
    if (left < padding) left = padding;
    if (left + tooltipRect.width > window.innerWidth - padding) {
      left = window.innerWidth - tooltipRect.width - padding;
    }
    if (top < padding) top = padding;
    if (top + tooltipRect.height > window.innerHeight - padding) {
      top = window.innerHeight - tooltipRect.height - padding;
    }

    setCoords({ top, left });
  };

  const show = () => {
    if (disabled) return;

    if (delay > 0) {
      timeoutRef.current = setTimeout(() => {
        setIsVisible(true);
      }, delay);
    } else {
      setIsVisible(true);
    }
  };

  const hide = () => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    setIsVisible(false);
  };

  const toggle = () => {
    if (disabled) return;
    setIsVisible(!isVisible);
  };

  useEffect(() => {
    if (isVisible) {
      calculatePosition();
      window.addEventListener('scroll', calculatePosition, true);
      window.addEventListener('resize', calculatePosition);
    }

    return () => {
      window.removeEventListener('scroll', calculatePosition, true);
      window.removeEventListener('resize', calculatePosition);
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [isVisible]);

  const triggerProps = trigger === 'hover'
    ? {
        onMouseEnter: show,
        onMouseLeave: hide,
        onFocus: show,
        onBlur: hide,
      }
    : {
        onClick: toggle,
      };

  const getArrowStyles = (): React.CSSProperties => {
    const arrowStyle: React.CSSProperties = {
      position: 'absolute',
      width: 0,
      height: 0,
      borderStyle: 'solid',
    };

    const color = darkMode ? '#1f2937' : '#ffffff';
    const borderColor = darkMode ? '#374151' : '#e5e7eb';

    switch (position) {
      case 'top':
        arrowStyle.bottom = '-6px';
        arrowStyle.left = '50%';
        arrowStyle.transform = 'translateX(-50%)';
        arrowStyle.borderWidth = '6px 6px 0 6px';
        arrowStyle.borderColor = `${color} transparent transparent transparent`;
        break;
      case 'bottom':
        arrowStyle.top = '-6px';
        arrowStyle.left = '50%';
        arrowStyle.transform = 'translateX(-50%)';
        arrowStyle.borderWidth = '0 6px 6px 6px';
        arrowStyle.borderColor = `transparent transparent ${color} transparent`;
        break;
      case 'left':
        arrowStyle.right = '-6px';
        arrowStyle.top = '50%';
        arrowStyle.transform = 'translateY(-50%)';
        arrowStyle.borderWidth = '6px 0 6px 6px';
        arrowStyle.borderColor = `transparent transparent transparent ${color}`;
        break;
      case 'right':
        arrowStyle.left = '-6px';
        arrowStyle.top = '50%';
        arrowStyle.transform = 'translateY(-50%)';
        arrowStyle.borderWidth = '6px 6px 6px 0';
        arrowStyle.borderColor = `transparent ${color} transparent transparent`;
        break;
    }

    return arrowStyle;
  };

  const tooltipElement = isVisible && (
    <div
      ref={tooltipRef}
      className={`
        fixed z-[9999] px-3 py-2 text-sm rounded-lg shadow-lg
        pointer-events-none transition-opacity duration-200
        ${darkMode
          ? 'bg-gray-800 text-white border border-gray-700'
          : 'bg-white text-gray-900 border border-gray-200'
        }
        ${className}
      `}
      style={{
        top: `${coords.top}px`,
        left: `${coords.left}px`,
        opacity: coords.top === 0 && coords.left === 0 ? 0 : 1,
      }}
      role="tooltip"
      aria-hidden={!isVisible}
    >
      {content}
      {showArrow && <div style={getArrowStyles()} />}
    </div>
  );

  return (
    <>
      <div
        ref={triggerRef}
        className="inline-block"
        {...triggerProps}
        aria-describedby={isVisible ? 'tooltip' : undefined}
      >
        {children}
      </div>
      {typeof window !== 'undefined' && createPortal(tooltipElement, document.body)}
    </>
  );
};

export default Tooltip;
