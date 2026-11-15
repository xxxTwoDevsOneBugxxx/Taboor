export interface PaginationMetadata {
  pageNumber: number
  pageSize: number
  totalCount: number
  totalPages: number
  hasPreviousPage: boolean
  hasNextPage: boolean
}

export interface ApiResponse<T> {
  data: T
  message?: string
  success: boolean
  error?: string
  errors?: string[]
  statusCode?: number
  pagination?: PaginationMetadata
}
