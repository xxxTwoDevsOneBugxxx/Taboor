import * as React from 'react'
import {
  flexRender,
  getCoreRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
} from '@tanstack/react-table'
import type {
  ColumnDef,
  OnChangeFn,
  SortingState,
  VisibilityState,
} from '@tanstack/react-table'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import {
  DataTablePagination,
  type ServerPaginationConfig,
} from './DataTablePagination'
import { DataTableToolbar } from './DataTableToolbar'
import { useDirection } from '#/hooks/use-direction'
import { cn } from '@/lib/utils'

export interface ServerSortingConfig {
  sorting: SortingState
  onSortingChange: OnChangeFn<SortingState>
}

export interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[]
  data: TData[]
  isLoading?: boolean
  serverPagination?: ServerPaginationConfig
  serverSorting?: ServerSortingConfig
  searchConfig?: {
    value: string
    onChange: (value: string) => void
    placeholder?: string
  }
  filterSlot?: React.ReactNode
  actionSlot?: React.ReactNode
  onResetFilters?: () => void
  hasActiveFilters?: boolean
  emptyState?: React.ReactNode
  onRowClick?: (row: TData) => void
  onRowHover?: (row: TData) => void
  hoverIntentDelay?: number
  renderMobileItem?: (item: TData, index: number) => React.ReactNode
  autoScrollOnPageChange?: boolean
  className?: string
}

export function DataTable<TData, TValue>({
  columns,
  data,
  isLoading = false,
  serverPagination,
  serverSorting,
  searchConfig,
  filterSlot,
  actionSlot,
  onResetFilters,
  hasActiveFilters = false,
  emptyState,
  onRowClick,
  onRowHover,
  hoverIntentDelay = 120,
  renderMobileItem,
  autoScrollOnPageChange = true,
  className,
}: DataTableProps<TData, TValue>) {
  const direction = useDirection()
  const isRtl = direction === 'rtl'

  // Hover Intent Timer Management
  const hoverTimerRef = React.useRef<NodeJS.Timeout | null>(null)

  const handleRowMouseEnter = React.useCallback(
    (row: TData) => {
      if (!onRowHover) return
      if (hoverTimerRef.current) {
        clearTimeout(hoverTimerRef.current)
      }
      hoverTimerRef.current = setTimeout(() => {
        onRowHover(row)
        hoverTimerRef.current = null
      }, hoverIntentDelay)
    },
    [onRowHover, hoverIntentDelay],
  )

  const handleRowMouseLeave = React.useCallback(() => {
    if (hoverTimerRef.current) {
      clearTimeout(hoverTimerRef.current)
      hoverTimerRef.current = null
    }
  }, [])

  React.useEffect(() => {
    return () => {
      if (hoverTimerRef.current) {
        clearTimeout(hoverTimerRef.current)
      }
    }
  }, [])

  const isServerSorting = Boolean(serverSorting)
  const [internalSorting, setInternalSorting] = React.useState<SortingState>([])
  const sorting = isServerSorting ? serverSorting!.sorting : internalSorting
  const onSortingChange = isServerSorting
    ? serverSorting!.onSortingChange
    : setInternalSorting

  const [columnVisibility, setColumnVisibility] =
    React.useState<VisibilityState>({})
  const [rowSelection, setRowSelection] = React.useState({})

  const table = useReactTable({
    data,
    columns,
    state: {
      sorting,
      columnVisibility,
      rowSelection,
    },
    enableSortingRemoval: true,
    manualSorting: isServerSorting,
    manualPagination: Boolean(serverPagination),
    pageCount: serverPagination?.totalPages ?? -1,
    onSortingChange,
    onColumnVisibilityChange: setColumnVisibility,
    onRowSelectionChange: setRowSelection,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: isServerSorting ? undefined : getSortedRowModel(),
    getPaginationRowModel: serverPagination ? undefined : getPaginationRowModel(),
  })

  const hasToolbar = Boolean(searchConfig || filterSlot || actionSlot)

  // Auto-scroll to top of list/table on page change
  const containerRef = React.useRef<HTMLDivElement>(null)
  const isFirstRender = React.useRef(true)
  const pageIndex = table.getState().pagination.pageIndex
  const currentPage = serverPagination?.pageNumber ?? pageIndex
  const currentSize =
    serverPagination?.pageSize ?? table.getState().pagination.pageSize

  React.useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    if (autoScrollOnPageChange) {
      containerRef.current?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      })
    }
  }, [currentPage, currentSize, autoScrollOnPageChange])

  return (
    <div
      ref={containerRef}
      className={cn(
        'w-full scroll-mt-20',
        renderMobileItem
          ? 'space-y-3 md:space-y-0 md:rounded-xl md:border md:bg-card md:text-card-foreground md:shadow-xs md:overflow-hidden'
          : 'rounded-xl border bg-card text-card-foreground shadow-xs overflow-hidden',
        className,
      )}
    >
      {/* Optional Toolbar */}
      {hasToolbar && (
        <DataTableToolbar
          table={table}
          searchConfig={searchConfig}
          filterSlot={filterSlot}
          actionSlot={actionSlot}
          onResetFilters={onResetFilters}
          hasActiveFilters={hasActiveFilters}
          className={cn(
            renderMobileItem &&
              'rounded-xl border bg-card text-card-foreground shadow-2xs md:rounded-none md:border-0 md:border-b md:border-border/60 md:shadow-none',
          )}
        />
      )}

      {/* Desktop Table View */}
      <div
        className={cn(
          'relative overflow-x-auto',
          renderMobileItem ? 'hidden md:block' : 'block',
        )}
      >
        <Table>
          <TableHeader>
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <TableHead key={header.id} className="whitespace-nowrap">
                    {header.isPlaceholder
                      ? null
                      : flexRender(
                          header.column.columnDef.header,
                          header.getContext(),
                        )}
                  </TableHead>
                ))}
              </TableRow>
            ))}
          </TableHeader>

          <TableBody>
            {isLoading && data.length === 0 ? (
              // Initial Loading Skeleton Rows
              Array.from({ length: serverPagination?.pageSize ?? 5 }).map(
                (_, idx) => (
                  <TableRow key={`skeleton-${idx}`}>
                    {columns.map((col, colIdx) => (
                      <TableCell key={`skeleton-col-${colIdx}`} className="py-3">
                        <Skeleton className="h-5 w-full rounded-md" />
                      </TableCell>
                    ))}
                  </TableRow>
                ),
              )
            ) : table.getRowModel().rows?.length ? (
              // Real Data Rows
              table.getRowModel().rows.map((row) => (
                <TableRow
                  key={row.id}
                  data-state={row.getIsSelected() && 'selected'}
                  onMouseEnter={() => handleRowMouseEnter(row.original)}
                  onMouseLeave={handleRowMouseLeave}
                  onClick={(e) => {
                    const target = e.target as HTMLElement
                    if (
                      target.closest(
                        'button, a, input, select, [role="menuitem"], [data-slot="dropdown-menu-trigger"]',
                      )
                    ) {
                      return
                    }
                    onRowClick?.(row.original)
                  }}
                  className={cn(
                    'transition-opacity duration-150',
                    isLoading && 'opacity-60',
                    onRowClick && 'cursor-pointer hover:bg-muted/60',
                  )}
                >
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id} className="py-2.5">
                      {flexRender(
                        cell.column.columnDef.cell,
                        cell.getContext(),
                      )}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              // Empty State
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className="h-36 text-center text-muted-foreground"
                >
                  {emptyState ?? (
                    <div className="flex flex-col items-center justify-center gap-1.5 py-6">
                      <p className="text-sm font-medium">
                        {isRtl ? 'لا توجد بيانات متاحة' : 'No results found'}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {isRtl
                          ? 'جرب تغيير شروط البحث أو الفلاتر'
                          : 'Try adjusting your search or filters'}
                      </p>
                    </div>
                  )}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      {/* Mobile Responsive List View */}
      {renderMobileItem && (
        <div className="block md:hidden space-y-3">
          {isLoading && data.length === 0 ? (
            // Mobile Skeleton Loading Items (separated cards)
            Array.from({ length: serverPagination?.pageSize ?? 5 }).map(
              (_, idx) => (
                <div
                  key={`mobile-skeleton-${idx}`}
                  className="rounded-xl border bg-card p-3.5 space-y-3 shadow-2xs"
                >
                  <div className="flex items-center gap-2.5">
                    <Skeleton className="size-9 rounded-lg shrink-0" />
                    <div className="space-y-1.5 flex-1">
                      <Skeleton className="h-4 w-3/4" />
                      <Skeleton className="h-3 w-1/2" />
                    </div>
                  </div>
                  <Skeleton className="h-14 w-full rounded-lg" />
                  <div className="flex items-center justify-between pt-1">
                    <div className="flex gap-1.5">
                      <Skeleton className="h-5 w-14 rounded-md" />
                      <Skeleton className="h-5 w-14 rounded-md" />
                    </div>
                    <Skeleton className="h-4 w-20" />
                  </div>
                </div>
              ),
            )
          ) : table.getRowModel().rows?.length ? (
            // Mobile Real Cards (separated floating cards)
            table.getRowModel().rows.map((row) => (
              <div
                key={row.id}
                onMouseEnter={() => handleRowMouseEnter(row.original)}
                onMouseLeave={handleRowMouseLeave}
                onTouchStart={() => handleRowMouseEnter(row.original)}
                onClick={(e) => {
                  const target = e.target as HTMLElement
                  if (
                    target.closest(
                      'button, a, input, select, [role="menuitem"], [data-slot="dropdown-menu-trigger"]',
                    )
                  ) {
                    return
                  }
                  onRowClick?.(row.original)
                }}
                className={cn(
                  'rounded-xl border bg-card shadow-2xs overflow-hidden transition-all duration-150',
                  isLoading && 'opacity-60',
                  onRowClick &&
                    'cursor-pointer active:scale-[0.99] hover:border-primary/40 hover:shadow-xs',
                )}
              >
                {renderMobileItem(row.original, row.index)}
              </div>
            ))
          ) : (
            // Mobile Empty State (separated card)
            <div className="rounded-xl border bg-card flex flex-col items-center justify-center gap-1.5 py-12 px-4 text-center shadow-2xs">
              <p className="text-sm font-medium">
                {isRtl ? 'لا توجد بيانات متاحة' : 'No results found'}
              </p>
              <p className="text-xs text-muted-foreground">
                {isRtl
                  ? 'جرب تغيير شروط البحث أو الفلاتر'
                  : 'Try adjusting your search or filters'}
              </p>
            </div>
          )}
        </div>
      )}

      {/* Pagination Footer */}
      <DataTablePagination
        table={table}
        serverPagination={serverPagination}
        className={cn(
          renderMobileItem
            ? 'rounded-xl border bg-card text-card-foreground shadow-2xs md:rounded-none md:border-0 md:border-t md:border-border/60 md:shadow-none'
            : 'border-t',
        )}
      />
    </div>
  )
}
