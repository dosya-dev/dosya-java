package dev.dosya.sdk.resource;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.dosya.sdk.internal.DosyaHttpClient;
import dev.dosya.sdk.internal.HttpRequest;
import dev.dosya.sdk.model.CreateInviteLinkParams;
import dev.dosya.sdk.model.CreatedInviteLink;
import dev.dosya.sdk.model.InviteLink;
import dev.dosya.sdk.model.InviteMemberParams;
import dev.dosya.sdk.model.MemberUpdateResult;
import dev.dosya.sdk.model.TeamListResponse;
import dev.dosya.sdk.model.UpdateMemberParams;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static dev.dosya.sdk.internal.PathSegments.seg;

/**
 * Workspace members and invitations.
 *
 * <p>Key scope: GET needs {@code read} or {@code full}, the rest {@code full}; {@code upload}
 * keys cannot call these. Workspace-pinned keys can only call {@link #list(String)} and
 * {@link #listInviteLinks(String)} (for their own workspace); every other method is refused
 * for them (403).
 *
 * @since 0.3.0
 */
public final class TeamResource {

    private final DosyaHttpClient http;

    public TeamResource(@NotNull DosyaHttpClient http) {
        this.http = http;
    }

    /**
     * Members, pending invites (with accept URLs), recent activity and counts.
     * Needs {@code access_team} and {@code view_team_members}.
     */
    public @NotNull TeamListResponse list(@NotNull String workspaceId) {
        return http.requestAs(HttpRequest.get("/api/team").query("workspace_id", workspaceId),
                TeamListResponse.class);
    }

    /**
     * Invites someone by email (the invite expires in 7 days) and returns the invite id.
     *
     * <p>Needs {@code invite_members} and cannot grant a role above the caller's own (403).
     * 409 when already a member or already invited; 403 outside the workspace's allowed
     * email domains or past the plan's member limit. dosya.dev addresses are refused (400)
     * unless the caller is a dosya.dev account.
     */
    public @NotNull String invite(@NotNull InviteMemberParams params) {
        JsonObject resp = http.request(HttpRequest.post("/api/team/invite").body(params.toBody()));
        return resp.get("invite_id").getAsString();
    }

    /** Emails a pending invite again. 409 when accepted, revoked or expired; 503 when email is not configured. */
    public void resendInvite(@NotNull String inviteId) {
        http.request(HttpRequest.post("/api/team/invites/" + seg(inviteId) + "/resend"));
    }

    /** Revokes a pending invite. 409 when already revoked. */
    public void revokeInvite(@NotNull String inviteId) {
        http.request(HttpRequest.post("/api/team/invites/" + seg(inviteId) + "/revoke"));
    }

    /**
     * Changes a member's role and/or folder confinement. Fields not set are left alone.
     * Needs {@code manage_roles}. 400 for the owner, for your own role or confinement, or an
     * unknown role/folder; 403 for a role above your own.
     *
     * @param membershipId the membership id (see {@code TeamMember.getMembershipId()})
     */
    public @NotNull MemberUpdateResult updateMember(@NotNull String membershipId, @NotNull UpdateMemberParams params) {
        return http.requestAs(HttpRequest.put("/api/team/members/" + seg(membershipId)).body(params.toBody()),
                MemberUpdateResult.class);
    }

    /**
     * Removes a member. Needs {@code manage_roles} unless removing yourself; the owner cannot
     * be removed (400). Never retried.
     */
    public void removeMember(@NotNull String membershipId) {
        http.request(HttpRequest.delete("/api/team/members/" + seg(membershipId)).retry(HttpRequest.Retry.NEVER));
    }

    /** Active join links, with their URLs. Needs {@code invite_members}. */
    public @NotNull List<InviteLink> listInviteLinks(@NotNull String workspaceId) {
        JsonObject resp = http.request(HttpRequest.get("/api/team/invite-link").query("workspace_id", workspaceId));
        List<InviteLink> links = http.fromJson(resp.get("links"), new TypeToken<List<InviteLink>>() {}.getType());
        return links != null ? Collections.unmodifiableList(links) : Collections.emptyList();
    }

    /**
     * Creates a join link anyone holding it can use. Needs {@code invite_members} and cannot
     * grant a role above the caller's own.
     */
    public @NotNull CreatedInviteLink createInviteLink(@NotNull CreateInviteLinkParams params) {
        JsonObject resp = http.request(HttpRequest.post("/api/team/invite-link").body(params.toBody()));
        return http.fromJson(resp.get("link"), CreatedInviteLink.class);
    }

    /** Revokes a join link. */
    public void revokeInviteLink(@NotNull String linkId) {
        seg(linkId); // validates: an empty id would otherwise be dropped from the query
        http.request(HttpRequest.delete("/api/team/invite-link").query("id", linkId));
    }
}
