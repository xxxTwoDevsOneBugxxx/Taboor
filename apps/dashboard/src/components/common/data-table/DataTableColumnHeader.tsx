import type { Column } from '@tanstack/react-table'
import { ArrowDownIcon, ArrowUpIcon, ChevronsUpDownIcon } from 'lucide-react'
import { cn } from '@/lib/utils'

interface DataTableColumnHeaderProps<TData, TValue>
  extends React.HTMLAttributes<HTMLDivElement> {
  column: Column<TData, TValue>
  title: string
}

export function DataTableColumnHeader<TData, TValue>({
  column,
  title,
  className,
}: DataTableColumnHeaderProps<TData, TValue>) {
  if (!column.getCanSort()) {
    return (
      <span className={cn('text-xs font-medium text-muted-foreground', className)}>
        {title}
      </span>
    )
  }

  const isSorted = column.getIsSorted()

  const handleSort = () => {
    if (isSorted === 'asc') {
      column.toggleSorting(true) // Click 2: Descending
    } else if (isSorted === 'desc') {
      column.clearSorting() // Click 3: Reset to default (unsorted)
    } else {
      column.toggleSorting(false) // Click 1: Ascending
    }
  }

  return (
    <button
      type="button"
      onClick={handleSort}
      className={cn(
        'group inline-flex items-center gap-1.5 text-xs font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer select-none p-0 bg-transparent border-0 outline-none',
        className,
      )}
    >
      <span>{title}</span>
      {isSorted === 'desc' ? (
        <ArrowDownIcon className="size-3 text-primary shrink-0" />
      ) : isSorted === 'asc' ? (
        <ArrowUpIcon className="size-3 text-primary shrink-0" />
      ) : (
        <ChevronsUpDownIcon className="size-3 text-muted-foreground/40 group-hover:text-muted-foreground shrink-0 transition-colors" />
      )}
    </button>
  )
}
