package gov.cmr.minfi.db.gbe.app.auth.request;

import lombok.Builder;

@Builder
public record VerificationRequest(String email, String code) {
}
