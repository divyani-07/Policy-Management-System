// Pagination wrapper — contains the list + page info
package com.pms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class UserPageResponseDTO {
    private List<UserResponseDTO> users;   // actual data for this page
    private int currentPage;               // which page are we on (0-based)
    private int totalPages;                // how many pages exist total
    private long totalUsers;              // total count across all pages
    private int pageSize;                  // how many per page

}
