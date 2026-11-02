using System.Security.Cryptography;
using System.Text;

namespace Taboor_Application.Common.Security;

/// <summary>
/// Cryptographic utility for securely hashing sensitive token values before persisting them to the database.
/// </summary>
public static class TokenHasher
{
    /// <summary>
    /// Computes the SHA-256 hex digest of a token string.
    /// </summary>
    public static string Hash(string token)
    {
        ArgumentNullException.ThrowIfNull(token);

        var bytes = SHA256.HashData(Encoding.UTF8.GetBytes(token));
        return Convert.ToHexString(bytes);
    }
}
