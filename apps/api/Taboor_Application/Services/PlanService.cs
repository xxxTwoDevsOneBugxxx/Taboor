using AutoMapper;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using System.Net;
using Taboor_Application.Common;
using Taboor_Application.DTOs.Plan;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;
using Taboor_Domain.Repositories;
using Taboor_Domain.Repositories.IRepository;

namespace Taboor_Application.Services
{
    /// <summary>
    /// Service implementation for managing subscription plans.
    /// </summary>
    public class PlanService : IPlanService
    {
        private readonly IUnitOfWork _uow;
        private readonly IMapper _mapper;
        private readonly ILogger<PlanService> _logger;

        public PlanService(
            IUnitOfWork uow,
            IMapper mapper,
            ILogger<PlanService> logger)
        {
            _uow = uow ?? throw new ArgumentNullException(nameof(uow));
            _mapper = mapper ?? throw new ArgumentNullException(nameof(mapper));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task<ApiResponse<IReadOnlyList<PlanDTO>>> GetAllPlansAsync(CancellationToken cancellationToken = default)
        {
            try
            {
                _logger.LogInformation("Retrieving all plans");

                var repo = _uow.Repository<IPlanRepository>();
                var allPlans = await repo.ListAllAsync(cancellationToken);
                var plans = allPlans.OrderBy(p => p.Price).ToList();

                var dtos = _mapper.Map<IReadOnlyList<PlanDTO>>(plans);
                return ApiResponse<IReadOnlyList<PlanDTO>>.SuccessResponse(dtos);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while retrieving plans");
                return ApiResponse<IReadOnlyList<PlanDTO>>.FailResponse("Failed to retrieve plans.");
            }
        }

        public async Task<ApiResponse<PlanDTO>> GetPlanByIdAsync(int id, CancellationToken cancellationToken = default)
        {
            try
            {
                _logger.LogInformation("Retrieving plan by ID: {Id}", id);

                var plan = await _uow.Repository<IPlanRepository>().GetByIdAsync(id, cancellationToken);
                if (plan == null)
                {
                    _logger.LogWarning("Plan with ID {Id} not found", id);
                    var response = ApiResponse<PlanDTO>.FailResponse("Plan not found.");
                    response.StatusCode = HttpStatusCode.NotFound;
                    return response;
                }

                var dto = _mapper.Map<PlanDTO>(plan);
                return ApiResponse<PlanDTO>.SuccessResponse(dto);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while retrieving plan with ID: {Id}", id);
                return ApiResponse<PlanDTO>.FailResponse("Failed to retrieve plan.");
            }
        }

        public async Task<ApiResponse<PlanDTO>> CreatePlanAsync(CreatePlanDTO dto, CancellationToken cancellationToken = default)
        {
            try
            {
                if (dto == null)
                {
                    return ApiResponse<PlanDTO>.FailResponse("Plan data is required.");
                }

                _logger.LogInformation("Creating plan: {Name}", dto.Name);

                if (dto.MaxServicesPerBranch > dto.MaxServices)
                {
                    var response = ApiResponse<PlanDTO>.FailResponse(
                        "MaxServicesPerBranch cannot exceed MaxServices.",
                        new List<string> { "MaxServicesPerBranch must be less than or equal to MaxServices." }
                    );
                    response.StatusCode = HttpStatusCode.BadRequest;
                    return response;
                }

                var repo = _uow.Repository<IPlanRepository>();
                if (await repo.NameExistsAsync(dto.Name, cancellationToken: cancellationToken))
                {
                    _logger.LogWarning("Plan creation failed: name '{Name}' already exists", dto.Name);
                    var response = ApiResponse<PlanDTO>.FailResponse(
                        "A plan with this name already exists.",
                        new List<string> { "Plan name must be unique." }
                    );
                    response.StatusCode = HttpStatusCode.Conflict;
                    return response;
                }

                var plan = Plan.Create(dto.Name, dto.Description, dto.Price, dto.BillingCycle, dto.MaxBranches, dto.MaxServices, dto.MaxServicesPerBranch);


                plan.Activate();

                await repo.AddAsync(plan, cancellationToken);
                await _uow.SaveAsync();

                _logger.LogInformation("Plan created successfully with ID: {Id}", plan.Id);

                var resultDto = _mapper.Map<PlanDTO>(plan);
                var successResponse = ApiResponse<PlanDTO>.SuccessResponse(resultDto, "Plan created successfully.");
                successResponse.StatusCode = HttpStatusCode.Created;
                return successResponse;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while creating plan: {Name}", dto?.Name);
                return ApiResponse<PlanDTO>.FailResponse("Failed to create plan.");
            }
        }

        public async Task<ApiResponse<PlanDTO>> UpdatePlanAsync(int id, UpdatePlanDTO dto, CancellationToken cancellationToken = default)
        {
            try
            {
                if (dto == null)
                {
                    return ApiResponse<PlanDTO>.FailResponse("Plan data is required.");
                }

                _logger.LogInformation("Updating plan ID: {Id}", id);

                if (dto.MaxServicesPerBranch > dto.MaxServices)
                {
                    var response = ApiResponse<PlanDTO>.FailResponse(
                        "MaxServicesPerBranch cannot exceed MaxServices.",
                        new List<string> { "MaxServicesPerBranch must be less than or equal to MaxServices." }
                    );
                    response.StatusCode = HttpStatusCode.BadRequest;
                    return response;
                }

                var repo = _uow.Repository<IPlanRepository>();
                var plan = await repo.GetByIdAsync(id, cancellationToken);
                if (plan == null)
                {
                    _logger.LogWarning("Plan with ID {Id} not found for update", id);
                    var response = ApiResponse<PlanDTO>.FailResponse("Plan not found.");
                    response.StatusCode = HttpStatusCode.NotFound;
                    return response;
                }

                if (await repo.NameExistsAsync(dto.Name, excludeId: id, cancellationToken: cancellationToken))
                {
                    _logger.LogWarning("Plan update failed: name '{Name}' already exists on another plan", dto.Name);
                    var response = ApiResponse<PlanDTO>.FailResponse(
                        "Another plan with this name already exists.",
                        new List<string> { "Plan name must be unique." }
                    );
                    response.StatusCode = HttpStatusCode.Conflict;
                    return response;
                }


                plan.Update(dto.Name, dto.Description, dto.Price, dto.BillingCycle, dto.MaxBranches, dto.MaxServices, dto.MaxServicesPerBranch);







                await repo.UpdateAsync(plan, cancellationToken);
                await _uow.SaveAsync();

                _logger.LogInformation("Plan ID {Id} updated successfully", id);

                var resultDto = _mapper.Map<PlanDTO>(plan);
                return ApiResponse<PlanDTO>.SuccessResponse(resultDto, "Plan updated successfully.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while updating plan with ID: {Id}", id);
                return ApiResponse<PlanDTO>.FailResponse("Failed to update plan.");
            }
        }

        public async Task<ApiResponse<object>> ActivatePlanAsync(int id, CancellationToken cancellationToken = default)
        {
            try
            {
                _logger.LogInformation("Activating plan ID: {Id}", id);

                var repo = _uow.Repository<IPlanRepository>();
                var plan = await repo.GetByIdAsync(id, cancellationToken);
                if (plan == null)
                {
                    _logger.LogWarning("Plan with ID {Id} not found for activation", id);
                    var response = ApiResponse<object>.FailResponse("Plan not found.");
                    response.StatusCode = HttpStatusCode.NotFound;
                    return response;
                }

                plan.Activate();


                await repo.UpdateAsync(plan, cancellationToken);
                await _uow.SaveAsync();

                _logger.LogInformation("Plan ID {Id} activated successfully", id);
                return ApiResponse<object>.SuccessResponse(new { id = plan.Id, isActive = plan.IsActive }, "Plan activated successfully.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while activating plan with ID: {Id}", id);
                return ApiResponse<object>.FailResponse("Failed to activate plan.");
            }
        }

        public async Task<ApiResponse<object>> DeactivatePlanAsync(int id, CancellationToken cancellationToken = default)
        {
            try
            {
                _logger.LogInformation("Deactivating plan ID: {Id}", id);

                var repo = _uow.Repository<IPlanRepository>();
                var plan = await repo.GetByIdAsync(id, cancellationToken);
                if (plan == null)
                {
                    _logger.LogWarning("Plan with ID {Id} not found for deactivation", id);
                    var response = ApiResponse<object>.FailResponse("Plan not found.");
                    response.StatusCode = HttpStatusCode.NotFound;
                    return response;
                }

                plan.Deactivate();


                await repo.UpdateAsync(plan, cancellationToken);
                await _uow.SaveAsync();

                _logger.LogInformation("Plan ID {Id} deactivated successfully", id);
                return ApiResponse<object>.SuccessResponse(new { id = plan.Id, isActive = plan.IsActive }, "Plan deactivated successfully.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while deactivating plan with ID: {Id}", id);
                return ApiResponse<object>.FailResponse("Failed to deactivate plan.");
            }
        }

        public async Task<ApiResponse<object>> DeletePlanAsync(int id, CancellationToken cancellationToken = default)
        {
            try
            {
                _logger.LogInformation("Deleting plan ID: {Id}", id);

                var repo = _uow.Repository<IPlanRepository>();
                var plan = await repo.GetByIdAsync(id, cancellationToken);
                if (plan == null)
                {
                    _logger.LogWarning("Plan with ID {Id} not found for deletion", id);
                    var response = ApiResponse<object>.FailResponse("Plan not found.");
                    response.StatusCode = HttpStatusCode.NotFound;
                    return response;
                }

                // 1. Business Rule: Only inactive plans can be deleted
                if (plan.IsActive)
                {
                    _logger.LogWarning("Deletion rejected: Plan ID {Id} is active", id);
                    var response = ApiResponse<object>.FailResponse(
                        "Only inactive plans can be deleted. Please deactivate the plan first.",
                        new List<string> { "Active plans cannot be deleted." }
                    );
                    response.StatusCode = HttpStatusCode.BadRequest;
                    return response;
                }

                // 2. Relational Check: Plan must have no associated subscriptions
                if (await repo.HasSubscriptionsAsync(id, cancellationToken))
                {
                    _logger.LogWarning("Deletion rejected: Plan ID {Id} has associated subscriptions", id);
                    var response = ApiResponse<object>.FailResponse(
                        "Cannot delete this plan because it has associated subscriptions.",
                        new List<string> { "Plans with active or historical subscriptions cannot be deleted." }
                    );
                    response.StatusCode = HttpStatusCode.Conflict;
                    return response;
                }

                await repo.DeleteAsync(plan, cancellationToken);
                await _uow.SaveAsync();

                _logger.LogInformation("Plan ID {Id} deleted successfully", id);
                return ApiResponse<object>.SuccessResponse(null, "Plan deleted successfully.");
            }
            catch (DbUpdateException ex)
            {
                _logger.LogError(ex, "Database constraint violation while deleting plan with ID: {Id}", id);
                var response = ApiResponse<object>.FailResponse(
                    "Cannot delete this plan due to database foreign key constraints.",
                    new List<string> { "This plan is referenced by other database records." }
                );
                response.StatusCode = HttpStatusCode.Conflict;
                return response;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while deleting plan with ID: {Id}", id);
                return ApiResponse<object>.FailResponse("Failed to delete plan.");
            }
        }
    }
}
