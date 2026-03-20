# Phân Tích và Hướng Dẫn Sửa Lỗi Appointment Report

## 1. Phân Tích Lỗi Backend

### Vấn đề

Lỗi `MethodArgumentTypeMismatchException` xảy ra khi backend nhận được một giá trị `String` là `"undefined"` cho một tham số mong đợi kiểu `Long` (ví dụ: `appointmentId`, `patientId`).

### Nguyên nhân

Lỗi này xuất phát từ frontend. Khi component `AppointmentReport` được render lần đầu, state `appointmentData` vẫn là `null`. Tuy nhiên, `useEffect` dùng để fetch reports và kiểm tra sự tồn tại của report đã được kích hoạt ngay lập tức.

```javascript
//...
const [appointmentData, setAppointmentData] = useState<any>(null);
//...

useEffect(() => {
    // Tại lần render đầu tiên, appointmentData là null
    // Do đó, appointmentData?.id và appointmentData?.patientId là undefined
    isReportExists(appointmentData?.id).then(/* ... */);
    getReportsByPatientId(appointmentData?.patientId).then(/* ... */);
}, [appointmentData?.patientId, appointmentData?.id]);
```

Các hàm service `isReportExists` và `getReportsByPatientId` đã gọi API với giá trị `undefined` trong URL (ví dụ: `/api/reports/exists/undefined`), dẫn đến lỗi ở phía Spring Boot.

### Giải pháp

Chỉ gọi các API này khi đã có `appointmentData` và các ID cần thiết.

```javascript
useEffect(() => {
    const fetchReportsAndCheckExistence = async () => {
        if (appointmentData?.id && appointmentData?.patientId) {
            setFetching(true);
            try {
                const [reports, reportExists] = await Promise.all([
                    getReportsByPatientId(appointmentData.patientId),
                    isReportExists(appointmentData.id)
                ]);
                setData(reports || []);
                setAllowAdd(!reportExists);
            } catch (err) {
                console.error('Failed to load reports or check existence:', err);
                errorNotification('Failed to load report data.');
                setData([]);
                setAllowAdd(true); // Hoặc false tùy vào logic mong muốn khi có lỗi
            } finally {
                setFetching(false);
            }
        }
    };

    fetchReportsAndCheckExistence();
}, [appointmentData]); // Chỉ phụ thuộc vào appointmentData
```

**Lưu ý:** Tôi đã sửa lại logic này trong file `AppointmentReport.tsx` cho bạn.

## 2. Phân Tích Logic Add/Edit Report (Frontend)

### Luồng hoạt động

1.  **Hiển thị ban đầu**:
    *   Component hiển thị một `DataTable` liệt kê các báo cáo cũ của bệnh nhân.
    *   Một nút **"Add Report"** được hiển thị nếu `allowAdd` là `true`.
    *   State `allowAdd` được quyết định bởi API `isReportExists(appointmentId)`. Nếu báo cáo cho cuộc hẹn này chưa tồn tại, `allowAdd` sẽ là `true`.

2.  **Thêm mới (Add)**:
    *   Khi người dùng nhấn nút **"Add Report"**, state `edit` được set thành `true`.
    *   Giao diện chuyển từ `DataTable` sang form nhập liệu chi tiết (`<Box component="form">`).

3.  **Sửa (Edit)**:
    *   **Hiện tại, code của bạn chưa có logic cho việc "Edit" một report đã tồn tại.** `DataTable` chỉ có action "View" (`IconEye`) để xem chi tiết cuộc hẹn, không có action "Edit".
    *   Nút "Add Report" cũng bị ẩn đi (`allowAdd` là `false`) nếu report đã tồn tại.

### Đề xuất để hoàn thiện tính năng Edit

Để cho phép chỉnh sửa, bạn cần:

1.  **Thêm Action "Edit"**: Thêm một `ActionIcon` với `IconPencil` vào `activityBodyTemplate` trong `DataTable`.
2.  **Xử lý sự kiện Edit**:
    *   Khi nhấn nút "Edit", gọi một hàm để lấy dữ liệu chi tiết của report đó từ backend.
    *   Dùng `form.setValues(...)` để điền dữ liệu đã lấy vào form.
    *   Set `setEdit(true)` để hiển thị form chỉnh sửa.
3.  **Cập nhật API**:
    *   Tạo một endpoint `PUT /api/appointment-records/{recordId}` ở backend để xử lý việc cập nhật.
    *   Trong `handleSubmit`, kiểm tra xem form đang ở chế độ thêm mới hay chỉnh sửa (bằng cách kiểm tra xem có `recordId` hay không) để gọi API `createAppointmentRecord` hoặc `updateAppointmentRecord`.

## 3. Tổng kết

- **Lỗi Backend**: Đã được khắc phục bằng cách sửa đổi logic gọi API ở frontend để đảm bảo `appointmentId` và `patientId` luôn có giá trị hợp lệ.
- **Logic Frontend**:
    - Luồng **Add** đã hoạt động đúng.
    - Luồng **Edit** chưa được cài đặt. Bạn cần bổ sung thêm logic để hoàn thiện tính năng này.

Các thay đổi cần thiết đã được áp dụng vào code. Bạn có thể kiểm tra lại và tiếp tục phát triển tính năng chỉnh sửa.

