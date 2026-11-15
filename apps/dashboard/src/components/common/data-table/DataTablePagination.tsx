import type { Table } from '@tanstack/react-table'
import {
  CheckIcon,
  ChevronDownIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ChevronsLeftIcon,
  ChevronsRightIcon,
} from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { cn } from '@/lib/utils'
import { useDirection } from '#/hooks/use-direction'

export interface ServerPaginationConfig {
  pageNumber: number
  pageSize: number
  totalCount: number
  totalPages: number
  onPageChange: (page: number) => void
  onPageSizeChange: (pageSize: number) => void
}

interface DataTablePaginationProps<TData> {
  table: Table<TData>
  serverPagination?: ServerPaginationConfig
  pageSizeOptions?: number[]
  className?: string
}

export function DataTablePagination<TData>({
  table,
  serverPagination,
  pageSizeOptions = [10, 20, 30, 50],
  className,
}: DataTablePaginationProps<TData>) {
  const { t } = useTranslation('common')
  const direction = useDirection()
  const isRtl = direction === 'rtl'

  const isServer = Boolean(serverPagination)
  const pageNumber = isServer
    ? serverPagination!.pageNumber
    : table.getState().pagination.pageIndex + 1
  const pageSize = isServer
    ? serverPagination!.pageSize
    : table.getState().pagination.pageSize
  const totalPages = isServer
    ? Math.max(serverPagination!.totalPages, 1)
    : Math.max(table.getPageCount(), 1)
  const totalCount = isServer
    ? serverPagination!.totalCount
    : table.getFilteredRowModel().rows.length

  const canPrevious = pageNumber > 1
  const canNext = pageNumber < totalPages

  const handleFirstPage = () => {
    if (isServer) {
      serverPagination!.onPageChange(1)
    } else {
      table.setPageIndex(0)
    }
  }

  const handlePreviousPage = () => {
    if (isServer) {
      serverPagination!.onPageChange(pageNumber - 1)
    } else {
      table.previousPage()
    }
  }

  const handleNextPage = () => {
    if (isServer) {
      serverPagination!.onPageChange(pageNumber + 1)
    } else {
      table.nextPage()
    }
  }

  const handleLastPage = () => {
    if (isServer) {
      serverPagination!.onPageChange(totalPages)
    } else {
      table.setPageIndex(totalPages - 1)
    }
  }

  const handlePageSizeChange = (newSize: number) => {
    if (isServer) {
      serverPagination!.onPageSizeChange(newSize)
    } else {
      table.setPageSize(newSize)
    }
  }

  const renderPageSizeDropdown = (compact = false) => (
    <DropdownMenu>
      <DropdownMenuTrigger
        render={
          <Button
            variant="outline"
            size="sm"
            className={cn(
              'justify-between text-xs font-normal gap-1 hover:bg-secondary/60 hover:text-secondary-foreground',
              compact ? 'h-7 min-w-[52px] px-2' : 'h-8 min-w-[64px] px-2.5',
            )}
          />
        }
      >
        <span>{pageSize}</span>
        <ChevronDownIcon className="size-3 text-muted-foreground opacity-60 shrink-0" />
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align={compact ? 'start' : 'end'}
        className={cn(
          'w-20 p-1 text-xs',
          compact ? 'min-w-[52px]' : 'min-w-[64px]',
        )}
      >
        {pageSizeOptions.map((size) => (
          <DropdownMenuItem
            key={size}
            onClick={() => handlePageSizeChange(size)}
            className="flex items-center justify-between text-xs cursor-pointer py-1.5"
          >
            <span>{size}</span>
            {size === pageSize && (
              <CheckIcon className="size-3.5 text-primary shrink-0 ms-1.5" />
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )

  return (
    <div
      className={cn(
        'flex items-center justify-between gap-2 px-3 py-2 text-xs text-muted-foreground',
        className,
      )}
    >
      {/* Start: Mobile (Dropdown + Total) | Desktop (Total count info) */}
      <div className="flex items-center gap-2 shrink-0">
        {/* Desktop Total Records Info */}
        <div className="hidden sm:flex items-center gap-1.5 whitespace-nowrap">
          <span>
            {isRtl
              ? `إجمالي ${totalCount} عنصر`
              : `Total ${totalCount} items`}
          </span>
          {table.getFilteredSelectedRowModel().rows.length > 0 && (
            <span className="text-primary font-medium">
              (
              {isRtl
                ? `تم تحديد ${table.getFilteredSelectedRowModel().rows.length}`
                : `${table.getFilteredSelectedRowModel().rows.length} selected`}
              )
            </span>
          )}
        </div>

        {/* Mobile Compact Page Size Dropdown + Total Count */}
        <div className="flex sm:hidden items-center gap-1.5">
          {renderPageSizeDropdown(true)}
          <span className="text-[11px] text-muted-foreground/80 whitespace-nowrap">
            {isRtl ? `${totalCount} عنصر` : `${totalCount} items`}
          </span>
        </div>
      </div>

      {/* End: Desktop (Page Size) + Page Info + Navigation Buttons */}
      <div className="flex items-center gap-2 sm:gap-6">
        {/* Desktop Page Size Selector */}
        <div className="hidden sm:flex items-center gap-2">
          <span className="whitespace-nowrap">
            {isRtl ? 'الصفوف في الصفحة:' : 'Rows per page:'}
          </span>
          {renderPageSizeDropdown(false)}
        </div>

        {/* Current Page Display */}
        <div className="flex items-center justify-center font-medium text-foreground text-xs whitespace-nowrap">
          {isRtl
            ? `صفحة ${pageNumber} من ${totalPages}`
            : `Page ${pageNumber} of ${totalPages}`}
        </div>

        {/* Navigation Buttons (RTL flipped) */}
        <div className="flex items-center gap-1">
          {/* First page (desktop only) */}
          <Button
            variant="outline"
            size="icon-sm"
            onClick={handleFirstPage}
            disabled={!canPrevious}
            className="hidden sm:inline-flex"
            title={isRtl ? 'الصفحة الأولى' : 'First page'}
          >
            {isRtl ? (
              <ChevronsRightIcon className="size-3.5" />
            ) : (
              <ChevronsLeftIcon className="size-3.5" />
            )}
          </Button>

          {/* Previous page */}
          <Button
            variant="outline"
            size="icon-sm"
            onClick={handlePreviousPage}
            disabled={!canPrevious}
            title={isRtl ? 'الصفحة السابقة' : 'Previous page'}
          >
            {isRtl ? (
              <ChevronRightIcon className="size-3.5" />
            ) : (
              <ChevronLeftIcon className="size-3.5" />
            )}
          </Button>

          {/* Next page */}
          <Button
            variant="outline"
            size="icon-sm"
            onClick={handleNextPage}
            disabled={!canNext}
            title={isRtl ? 'الصفحة التالية' : 'Next page'}
          >
            {isRtl ? (
              <ChevronLeftIcon className="size-3.5" />
            ) : (
              <ChevronRightIcon className="size-3.5" />
            )}
          </Button>

          {/* Last page (desktop only) */}
          <Button
            variant="outline"
            size="icon-sm"
            onClick={handleLastPage}
            disabled={!canNext}
            className="hidden sm:inline-flex"
            title={isRtl ? 'الصفحة الأخيرة' : 'Last page'}
          >
            {isRtl ? (
              <ChevronsLeftIcon className="size-3.5" />
            ) : (
              <ChevronsRightIcon className="size-3.5" />
            )}
          </Button>
        </div>
      </div>
    </div>
  )
}
