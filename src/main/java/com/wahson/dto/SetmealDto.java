package com.wahson.dto;

import com.wahson.entity.Setmeal;
import com.wahson.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
