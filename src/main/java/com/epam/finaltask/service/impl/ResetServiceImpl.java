package com.epam.finaltask.service.impl;

import com.epam.finaltask.model.ResetToken;
import com.epam.finaltask.service.ResetService;
import com.epam.finaltask.service.TokenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetServiceImpl implements ResetService {
    private final TokenStorageService<ResetToken> resetTokenStorageService;


    @Override
    public void proceedReset(String email, boolean isApi) {
    }

    @Override
    public ResetToken getResetToken(String token) {
        return resetTokenStorageService.get(token);
    }

    @Override
    public void removeResetToken(String token) {
        resetTokenStorageService.revoke(token);
    }
}
