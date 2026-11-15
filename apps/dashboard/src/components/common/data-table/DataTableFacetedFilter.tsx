import * as React from 'react'
import { CheckIcon, ChevronDownIcon } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { cn } from '@/lib/utils'

export interface FilterOption {
  value: string
  label: string
  icon?: LucideIcon | React.ComponentType<{ className?: string }>
  iconClassName?: string
  badgeClassName?: string
  itemClassName?: string
  dotClassName?: string
}

interface DataTableFacetedFilterProps {
  title: string
  value: string
  onChange: (value: string) => void
  options: FilterOption[]
  icon?: LucideIcon
  className?: string
  clearLabel?: string
}

export function DataTableFacetedFilter({
  title,
  value,
  onChange,
  options,
  icon: Icon,
  className,
  clearLabel = 'Clear filter',
}: DataTableFacetedFilterProps) {
  const selectedOption = options.find((opt) => opt.value === value)

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        render={
          <Button
            variant="outline"
            size="sm"
            className={cn(
              'h-8 border-dashed text-xs font-normal gap-1.5 px-2.5 transition-colors',
              'hover:bg-secondary/60 hover:text-secondary-foreground hover:border-secondary-foreground/30',
              value
                ? 'border-solid border-primary/30 bg-secondary/50 text-foreground font-medium'
                : 'text-muted-foreground',
              className,
            )}
          />
        }
      >
        {Icon && <Icon className="size-3.5 text-muted-foreground shrink-0" />}
        <span>{title}</span>
        {selectedOption && (
          <>
            <span className="h-3.5 w-px bg-border/80 mx-0.5 shrink-0" />
            <span
              className={cn(
                'inline-flex items-center gap-1 rounded-sm px-1.5 py-0.5 text-[11px] font-medium border shadow-2xs',
                selectedOption.badgeClassName ??
                  'bg-background/90 text-foreground border-border/50',
              )}
            >
              {selectedOption.dotClassName && (
                <span
                  className={cn(
                    'size-1.5 rounded-full shrink-0',
                    selectedOption.dotClassName,
                  )}
                />
              )}
              {selectedOption.icon && !selectedOption.dotClassName && (
                <selectedOption.icon
                  className={cn(
                    'size-3 shrink-0',
                    selectedOption.iconClassName,
                  )}
                />
              )}
              <span>{selectedOption.label}</span>
            </span>
          </>
        )}
        <ChevronDownIcon className="size-3 text-muted-foreground opacity-60 ms-0.5 shrink-0" />
      </DropdownMenuTrigger>

      <DropdownMenuContent align="start" className="w-52 p-1 text-xs">
        {options.map((option) => {
          const isSelected = option.value === value
          const OptionIcon = option.icon

          return (
            <DropdownMenuItem
              key={option.value}
              onClick={() => onChange(option.value)}
              className="flex items-center justify-between text-xs cursor-pointer py-1.5 group"
            >
              <div className="flex items-center gap-2 min-w-0">
                {option.dotClassName && (
                  <span
                    className={cn(
                      'size-2 rounded-full shrink-0',
                      option.dotClassName,
                    )}
                  />
                )}
                {OptionIcon && !option.dotClassName && (
                  <OptionIcon
                    className={cn(
                      'size-3.5 shrink-0 transition-colors',
                      option.iconClassName ?? 'text-muted-foreground',
                    )}
                  />
                )}
                <span
                  className={cn('truncate font-medium', option.itemClassName)}
                >
                  {option.label}
                </span>
              </div>
              {isSelected && (
                <CheckIcon className="size-3.5 text-primary shrink-0 ms-2" />
              )}
            </DropdownMenuItem>
          )
        })}

        {value && (
          <>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={() => onChange('')}
              className="justify-center text-xs text-muted-foreground cursor-pointer font-medium hover:text-foreground"
            >
              {clearLabel}
            </DropdownMenuItem>
          </>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
