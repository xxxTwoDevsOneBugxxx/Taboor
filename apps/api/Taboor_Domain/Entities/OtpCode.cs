namespace Taboor_Domain.Entities
{
    /// <summary>
    /// Represents a one-time password (OTP) code stored in the database.
    /// </summary>
    public class OtpCode
    {
        public int Id { get; set; }

        public string Email { get; set; } = string.Empty;

        public string Code { get; set; } = string.Empty;

        public OtpPurpose Purpose { get; set; }

        public DateTime CreatedAt { get; set; }

        public DateTime ExpiresAt { get; set; }

        public bool IsUsed { get; set; }

        public bool IsVerified { get; set; }

        public static OtpCode Create(string email, string code, OtpPurpose purpose, int expiryMinutes = 10)
        {
            return new OtpCode
            {
                Email = email.Trim().ToLower(),
                Code = code,
                Purpose = purpose,
                CreatedAt = DateTime.UtcNow,
                ExpiresAt = DateTime.UtcNow.AddMinutes(expiryMinutes),
                IsUsed = false,
                IsVerified = false
            };
        }

        public void MarkAsVerified(int extendedMinutes = 30)
        {
            IsVerified = true;
            ExpiresAt = DateTime.UtcNow.AddMinutes(extendedMinutes);
        }

        public void Invalidate()
        {
            IsUsed = true;
        }
    }
}
