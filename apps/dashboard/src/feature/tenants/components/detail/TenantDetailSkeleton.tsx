import { Skeleton } from '@/components/ui/skeleton'

export function TenantDetailSkeleton() {
  return (
    <div className="space-y-4">
      {/* Header Skeleton */}
      <div className="flex items-center gap-3 rounded-xl border bg-background/60 p-4">
        <Skeleton className="size-14 rounded-xl shrink-0" />
        <div className="space-y-2 flex-1">
          <Skeleton className="h-5 w-3/4" />
          <Skeleton className="h-3.5 w-1/2" />
          <div className="flex gap-2 pt-1">
            <Skeleton className="h-5 w-16 rounded-md" />
            <Skeleton className="h-5 w-16 rounded-md" />
          </div>
        </div>
      </div>

      {/* Cards Skeletons */}
      <div className="rounded-xl border bg-background/60 p-4 space-y-3">
        <Skeleton className="h-4 w-1/3" />
        <div className="grid grid-cols-2 gap-3 pt-2">
          <Skeleton className="h-8 w-full" />
          <Skeleton className="h-8 w-full" />
        </div>
      </div>

      <div className="rounded-xl border bg-background/60 p-4 space-y-3">
        <Skeleton className="h-4 w-1/3" />
        <Skeleton className="h-14 w-full" />
      </div>

      <div className="rounded-xl border bg-background/60 p-4 space-y-3">
        <Skeleton className="h-4 w-1/3" />
        <div className="space-y-2 pt-2">
          <Skeleton className="h-8 w-full" />
          <Skeleton className="h-8 w-full" />
        </div>
      </div>
    </div>
  )
}
