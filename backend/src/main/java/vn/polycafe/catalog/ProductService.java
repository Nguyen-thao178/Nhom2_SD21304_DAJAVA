package vn.polycafe.catalog;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.polycafe.auth.StaffUser;
import vn.polycafe.common.*;

@Service
public class ProductService {
    private final ProductRepository products;
    private final AuditService audit;

    public ProductService(ProductRepository products, AuditService audit) {
        this.products = products;
        this.audit = audit;
    }

    public void requireManager(StaffUser user) {
        if (!user.canManageProducts())
            throw new ApiException(403, "Chỉ chủ quán hoặc quản trị viên toàn hệ thống được sửa danh mục chung.");
    }

    public List<Product> list(String q, String status, int page, int size) {
        if (q.length() > 150 || page < 0 || page > 10000 || size < 1 || size > 100 || (!status.isEmpty()
                && !Set.of("active", "paused", "seasonal", "sold_out", "archived").contains(status)))
            throw new ApiException(400, "Bộ lọc hoặc phân trang không hợp lệ.");
        return products.list(q.trim(), status, page, size);
    }

    public Product get(UUID id) {
        return products.get(id, false).orElseThrow(() -> new ApiException(404, "Không tìm thấy sản phẩm."));
    }

    @Transactional
    public Product create(ProductInput input, StaffUser user) {
        requireManager(user);
        checkCategory(input, new UUID(0, 0));
        var id = products.insert(input);
        products.price(id, input.price(), products.databaseTime());
        var result = get(id);
        audit.record(user.id(), "product.create", "menu_item", id, null, result);
        return result;
    }

    @Transactional
    public Product update(UUID id, ProductInput input, StaffUser user) {
        return update(id, input, null, user);
    }

    @Transactional
    public Product update(UUID id, ProductInput input, java.time.OffsetDateTime expectedHeader, StaffUser user) {
        requireManager(user);
        var before = products.get(id, true).orElseThrow(() -> new ApiException(404, "Không tìm thấy sản phẩm."));
        var expected = expectedHeader != null ? expectedHeader : input.expectedUpdatedAt();
        if (expected == null || !expected.toInstant().equals(before.updatedAt().toInstant()))
            throw new ApiException(409, "Sản phẩm đã thay đổi. Hãy tải lại trước khi sửa.");
        checkCategory(input, id);
        products.update(id, input);
        if (before.price() == null || before.price().compareTo(input.price()) != 0)
            products.price(id, input.price(), products.databaseTime());
        var result = get(id);
        audit.record(user.id(), "product.update", "menu_item", id, before, result);
        return result;
    }

    @Transactional
    public void archive(UUID id, StaffUser user) {
        requireManager(user);
        var before = products.get(id, true).orElseThrow(() -> new ApiException(404, "Không tìm thấy sản phẩm."));
        if (before.status().equals("archived"))
            return;
        products.archive(id);
        audit.record(user.id(), "product.archive", "menu_item", id, before, get(id));
    }

    private void checkCategory(ProductInput p, UUID id) {
        if (products.categoryConflict(p.categoryCode(), p.categoryName().trim(), id))
            throw new ApiException(409, "Mã danh mục đã có tên khác. Hãy dùng đúng tên danh mục hiện tại.");
    }
}
