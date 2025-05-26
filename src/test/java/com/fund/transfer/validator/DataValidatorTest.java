package com.fund.transfer.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.demo.fund.transfer.exception.APIException;
import com.demo.fund.transfer.util.APIConstants;
import com.demo.fund.transfer.validator.BeanValidator;
import com.demo.fund.transfer.validator.DataValidator;
import com.demo.fund.transfer.validator.ValidatorFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
// import static org.mockito.Mockito.never; // Not used in the final version of the test

@RunWith(MockitoJUnitRunner.class)
public class DataValidatorTest {

    @InjectMocks
    private DataValidator dataValidator; // Not strictly needed as DataValidator only has a static method, but good for consistency.

    @Mock
    private BeanValidator mockBeanValidator;

    @Test
    public void testValidateData_nullInput_throwsAPIException() {
        APIException exception = assertThrows(APIException.class, () -> {
            DataValidator.validateData(null);
        });
        assertEquals(APIConstants.INVALID_PAYLOAD, exception.getCode());
        assertEquals(APIConstants.EMPTY_PAYLOAD_MSG, exception.getMessage());
    }

    @Test
    public void testValidateData_validInput_callsBeanValidator() throws APIException {
        Object validData = new Object();
        // Ensure ValidatorFactory is mocked for the static method call
        try (MockedStatic<ValidatorFactory> mockedFactory = Mockito.mockStatic(ValidatorFactory.class)) {
            mockedFactory.when(() -> ValidatorFactory.getBeanValidatorInstance(validData))
                         .thenReturn(mockBeanValidator);

            DataValidator.validateData(validData);

            // Verify that getBeanValidatorInstance was called with the validData
            mockedFactory.verify(() -> ValidatorFactory.getBeanValidatorInstance(validData));
            // Verify that validateBean was called on the mockBeanValidator instance
            verify(mockBeanValidator).validateBean();
        }
    }
    // The third test case `testValidateData_validatorFactoryReturnsNull_doesNotThrowNPE`
    // was intentionally removed as per the prompt's rationale.
}
