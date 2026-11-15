namespace Taboor_Application.Common.Models;

/// <summary>
/// Application-level container for paginated query results returned by CQRS handlers to driving adapters.
/// </summary>
/// <typeparam name="T">The type of item in the page.</typeparam>
public record PagedResult<T>(
    IReadOnlyList<T> Items,
    int PageNumber,
    int PageSize,
    int TotalCount
)
{
    public int TotalPages => PageSize > 0 ? (int)Math.Ceiling(TotalCount / (double)PageSize) : 0;
    public bool HasPreviousPage => PageNumber > 1;
    public bool HasNextPage => PageNumber < TotalPages;
}
