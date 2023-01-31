package bg.sofia.uni.fmi.mjt.battleships.common.response;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.ClientState;
import bg.sofia.uni.fmi.mjt.battleships.common.cookie.GameCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.list.ListUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ServerResponse {

    public ResponseStatus status;
    public String message;
    public ClientState cookies;
    public List<ServerResponse> signals;

    public ServerResponse(ResponseStatus status, String message, ClientState cookies, List<ServerResponse> signals) {
        this.status = status;
        this.message = message;
        this.cookies = cookies;
        this.signals = signals;
    }

    public ServerResponse(ResponseStatus status, String message, ClientState cookies) {
        this(status, message, cookies, null);
    }

    private ServerResponse(Builder builder) {
        this(builder.status, builder.message, builder.cookies, builder.signals);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ServerResponse castOther)) {
            return false;
        }

        return Objects.equals(this.message, castOther.message) &&
            this.status == castOther.status &&
            Objects.equals(this.cookies, castOther.cookies) &&
            ListUtil.haveSameElements(this.signals, castOther.signals);
    }

    public static class Builder {
        private ResponseStatus status;
        private String message;
        private ClientState cookies;
        private List<ServerResponse> signals;

        private Builder() {
            this.status = ResponseStatus.OK;
            this.message = null;
            this.cookies = null;
            this.signals = null;
        }

        public ServerResponse build() {
            return new ServerResponse(this);
        }

        public Builder setStatus(ResponseStatus status) {
            this.status = status;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setCookies(ClientState cookies) {
            this.cookies = cookies;
            return this;
        }

        public Builder setSignals(List<ServerResponse> signals) {
            this.signals = signals;
            return this;
        }
    }
}
