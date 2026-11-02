using System.ComponentModel.DataAnnotations.Schema;

namespace Taboor_Domain.Entities
{
    /// <summary>
    /// Represents a refresh token issued to a user for authentication
    /// </summary>
    public class RefreshToken
    {
        public int Id { get; set; }

        public string UserId { get; set; } = string.Empty;

        public string Token { get; set; } = string.Empty;

        public DateTime Expiry { get; set; }

        public DateTime CreatedAt { get; set; }

        public bool IsRevoked { get; set; }

        /// <summary>
        /// Indicates the token has already been used to obtain a new token.
        /// Used for single-use rotation and reuse (theft) detection.
        /// </summary>
        public bool IsUsed { get; set; }

        [ForeignKey("UserId")]
        public ApplicationUser User { get; set; } = null!;

        public static RefreshToken Create(string userId, string token, DateTime expiry)
        {
            return new RefreshToken
            {
                UserId = userId,
                Token = token,
                Expiry = expiry,
                CreatedAt = DateTime.UtcNow,
                IsRevoked = false,
                IsUsed = false
            };
        }

        public void MarkAsUsed() => IsUsed = true;

        public void Revoke() => IsRevoked = true;
    }
}
