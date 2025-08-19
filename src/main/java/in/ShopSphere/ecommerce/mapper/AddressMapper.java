package in.ShopSphere.ecommerce.mapper;

import in.ShopSphere.ecommerce.dto.address.AddressRequest;
import in.ShopSphere.ecommerce.dto.address.AddressResponse;
import in.ShopSphere.ecommerce.model.entity.Address;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toAddress(AddressRequest request);
    
    @Mapping(target = "user", source = "user")
    AddressResponse toAddressResponse(Address address);
    
    List<AddressResponse> toAddressResponseList(List<Address> addresses);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAddressFromRequest(AddressRequest request, @MappingTarget Address address);
    

}
