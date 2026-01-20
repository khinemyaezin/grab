package com.inventory.domain.exception;

import com.grab.framework.exception.DomainException;
import com.grab.framework.service.MessageSource;
import com.inventory.domain.config.SystemProperties;
import com.inventory.domain.enums.StockMovementType;
import lombok.Getter;

import java.util.Map;

@Getter
public class InvalidStockMovementTypeException extends DomainException {
    public InvalidStockMovementTypeException( StockMovementType stockMovementType) {
        super(new InvalidStockMomentTypeError(stockMovementType),
                "Invalid Stock Movement Type : " + stockMovementType);
    }

    record InvalidStockMomentTypeError( StockMovementType type) implements MessageSource {
        private static final String CODE = "exception.inventory.invalid_stock_movement_type_error";
        @Override
        public String code() {
            return CODE;
        }

        @Override
        public Map<String, Object> args() {
            return Map.of( "stockMovementType", type.name() );
        }
    }
}