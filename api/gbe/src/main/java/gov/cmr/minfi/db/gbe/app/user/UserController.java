package gov.cmr.minfi.db.gbe.app.user;

import gov.cmr.minfi.db.gbe.app.user.request.ChangePasswordRequest;
import gov.cmr.minfi.db.gbe.app.user.request.ProfileUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.Objects;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name="User", description ="User API")
public class UserController {
    private  final UserServices userServices;

    @PatchMapping("/me")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void updateProfileInfo(
            @RequestBody
            @Valid
            ProfileUpdateRequest request,
            final Authentication principal
            ){
        this.userServices.updateProfileInfo(request, getUserId(principal));
    }

    @PostMapping("/me/password")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            final Authentication principal
            ){
        this.userServices.changePassword(request, getUserId(principal));
    }

    @PatchMapping("/me/deactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void deactivateAccount( final Authentication principal){
        this.userServices.deactivateAccount(getUserId(principal));
    }

    @PatchMapping("/me/reactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void reactivateAccount( final Authentication principal){
        this.userServices.reactivateAccount(getUserId(principal));
    }

    @DeleteMapping("/me")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public  void deleteAccount( final Authentication principal){
        this.userServices.deleteAccount(getUserId(principal));
    }




    private String getUserId(final Authentication principal) {
        return  ((User) Objects.requireNonNull(principal.getPrincipal())).getId();
    }
}
