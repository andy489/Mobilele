package com.soft.mobilele.mapper;

import com.soft.mobilele.model.dto.BrandDto;
import com.soft.mobilele.model.dto.OfferAddDto;
import com.soft.mobilele.model.dto.UserRegistrationDto;
import com.soft.mobilele.model.entity.BrandEntity;
import com.soft.mobilele.model.entity.OfferEntity;
import com.soft.mobilele.model.entity.UserEntity;
import com.soft.mobilele.model.entity.UserRoleEntity;
import com.soft.mobilele.model.user.MobileleUserDetails;
import com.soft.mobilele.model.view.OfferDetailsView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Mapper(componentModel = "spring")
public interface MapStructMapper {

    @Mapping(target = "isActive", constant = "false")
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "id", ignore = true)
    UserEntity toEntity(UserRegistrationDto userRegisterDto);

    BrandDto toDto(BrandEntity brandEntity);

    @Mapping(source = "engineType", target = "engine")
    @Mapping(source = "transmissionType", target = "transmission")
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "modified", ignore = true)
    OfferEntity toEntity(OfferAddDto offerAddDto);

    @Mapping(target = "engineType", expression = "java( offerEntity.getEngine().getDisplayName() )")
    @Mapping(target = "transmissionType", expression = "java( offerEntity.getTransmission().getDisplayName() )")
    @Mapping(source = "model.name", target = "model")
    @Mapping(source = "model.brand.name", target = "brand")
    @Mapping(source = "seller.firstName", target = "sellerFirstName")
    @Mapping(source = "seller.lastName", target = "sellerLastName")
    @Mapping(target = "viewerIsOwner", ignore = true)
    OfferDetailsView toDetailsView(OfferEntity offerEntity);

    @Mapping(target = "authorities", ignore = true)
    MobileleUserDetails toUserDetails(UserEntity userEntity);

    default GrantedAuthority toGrantedAuthority(UserRoleEntity userRoleEntity){
        return new SimpleGrantedAuthority("ROLE_" + userRoleEntity.getUserRoleEnum().name());
    }

}
