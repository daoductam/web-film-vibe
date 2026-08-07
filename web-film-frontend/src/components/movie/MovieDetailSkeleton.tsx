import { Skeleton } from "../common/Skeleton";

export function MovieDetailSkeleton() {
  return (
    <div className="min-h-screen bg-obsidian text-text-primary">
      <main className="min-h-screen relative pb-20">
        <div className="relative z-20 pt-24 md:pt-28 max-w-[1600px] mx-auto px-4 md:px-6">
          {/* Breadcrumbs Skeleton */}
          <div className="flex gap-2 mb-6">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-4 w-4" />
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-4 w-4" />
            <Skeleton className="h-4 w-32" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
            {/* Main Content Skeleton */}
            <div className="lg:col-span-9 space-y-8">
              <div className="space-y-4">
                <Skeleton className="h-16 w-3/4" />
                <div className="flex gap-4">
                  <Skeleton className="h-5 w-20" />
                  <Skeleton className="h-5 w-20" />
                  <Skeleton className="h-5 w-20" />
                </div>
              </div>

              {/* Video Player Skeleton */}
              <Skeleton className="w-full aspect-video rounded-2xl" />

              {/* Episode Section Skeleton */}
              <Skeleton className="h-32 w-full rounded-xl" />

              <div className="grid md:grid-cols-3 gap-8 pt-4">
                <div className="md:col-span-2 space-y-6">
                  <Skeleton className="h-8 w-48" />
                  <div className="space-y-2">
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-2/3" />
                  </div>
                </div>
                <div className="space-y-6">
                  <Skeleton className="h-8 w-full" />
                  <div className="space-y-4">
                    <Skeleton className="h-12 w-full rounded-full" />
                    <Skeleton className="h-12 w-full rounded-full" />
                    <Skeleton className="h-12 w-full rounded-full" />
                  </div>
                </div>
              </div>
            </div>

            {/* Sidebar Skeleton */}
            <div className="lg:col-span-3 space-y-8">
              <Skeleton className="h-48 w-full rounded-2xl" />
              <div className="space-y-4">
                <Skeleton className="h-8 w-32" />
                <div className="space-y-4">
                  <Skeleton className="h-32 w-full" />
                  <Skeleton className="h-32 w-full" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
