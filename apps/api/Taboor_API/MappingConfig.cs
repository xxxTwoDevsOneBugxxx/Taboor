using AutoMapper;
using Taboor_Application.DTOs.Auth;
using Taboor_Domain.Entities;

namespace Taboor_API.MappingConfig
{
    /// <summary>
    /// AutoMapper configuration profile
    /// </summary>
    public class MappingConfig : Profile
    {
        /// <summary>
        /// Initializes a new instance of the MappingConfig class
        /// </summary>
        public MappingConfig()
        {
            #region User Mappings

            CreateMap<ApplicationUser, UserDTO>()
                .ForMember(dest => dest.Role, opt => opt.Ignore())
                .ReverseMap()
                .ForMember(dest => dest.UserName, opt => opt.MapFrom(src => src.Email));

            #endregion
        }
    }
}
