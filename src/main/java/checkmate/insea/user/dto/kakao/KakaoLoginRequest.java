package checkmate.insea.user.dto.kakao;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KakaoLoginRequest {
    @NotBlank(message = "인가 코드는 필수입니다")
    private String code;

    private String fcmToken;
    private String deviceType;
}
