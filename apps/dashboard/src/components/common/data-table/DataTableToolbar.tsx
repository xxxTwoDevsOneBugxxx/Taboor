import * as React from 'react'
import type { Table } from '@tanstack/react-table'
import { SearchIcon, XIcon } from 'lucide-react'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'
import { useDirection } from '#/hooks/use-direction'

interface DataTableToolbarProps<TData> {
  table: Table<TData>
  searchConfig?: {
    value: string
    onChange: (value: string) => void
    placeholder?: string
  }
  filterSlot?: React.ReactNode
  actionSlot?: React.ReactNode
  onResetFilters?: () => void
  hasActiveFilters?: boolean
  className?: string
}

export function DataTableToolbar<TData>({
  table,
  searchConfig,
  filterSlot,
  actionSlot,
  onResetFilters,
  hasActiveFilters,
  className,
}: DataTableToolbarProps<TData>) {
  const direction = useDirection()
  const isRtl = direction === 'rtl'

  return (
    <div
      className={cn(
        'flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 p-3',
        className,
      )}
    >
      {/* Search & Filters */}
      <div className="flex flex-1 flex-wrap items-center gap-2">
        {searchConfig && (
          <div className="relative w-full sm:w-64 md:w-72">
            <SearchIcon className="absolute start-2.5 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none" />
            <Input
              type="text"
              value={searchConfig.value}
              onChange={(e) => searchConfig.onChange(e.target.value)}
              placeholder={searchConfig.placeholder ?? (isRtl ? 'بحث...' : 'Search...')}
              className="ps-8 pe-8 h-8 text-xs"
            />
            {searchConfig.value && (
              <button
                type="button"
                onClick={() => searchConfig.onChange('')}
                className="absolute end-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                title={isRtl ? 'مسح البحث' : 'Clear search'}
              >
                <XIcon className="size-3.5" />
              </button>
            )}
          </div>
        )}

        {/* Custom Filter Slot (Status dropdowns, chips, etc.) */}
        {filterSlot}

        {/* Reset Button */}
        {hasActiveFilters && onResetFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onResetFilters}
            className="h-8 px-2 text-xs text-muted-foreground hover:text-foreground"
          >
            <XIcon className="size-3.5 me-1" />
            {isRtl ? 'إعادة ضبط' : 'Reset'}
          </Button>
        )}
      </div>

      {/* Actions Slot (e.g., Export, Create button, View options) */}
      {actionSlot && <div className="flex items-center gap-2">{actionSlot}</div>}
    </div>
  )
}
