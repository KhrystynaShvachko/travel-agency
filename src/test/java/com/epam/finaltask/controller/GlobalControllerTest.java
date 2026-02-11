package com.epam.finaltask.controller;

import com.epam.finaltask.contoller.GlobalController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalController Tests")
class GlobalControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private GlobalController globalController;

    @Test
    @DisplayName("index - Should return index page")
    void index_ReturnsIndexPage() {
        String result = globalController.index(model);

        assertEquals("index", result);
    }

    @Test
    @DisplayName("index2 - Should return index page for root path")
    void index2_ReturnsIndexPage() {
        String result = globalController.index2(model);

        assertEquals("index", result);
    }

    @Test
    @DisplayName("Both index methods should return same view")
    void bothIndexMethods_ReturnSameView() {
        String result1 = globalController.index(model);
        String result2 = globalController.index2(model);

        assertEquals(result1, result2);
        assertEquals("index", result1);
    }
}
