package com.example.xiangshouzhengdao.ui.profile

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.databinding.FragmentProfileBinding
import com.example.xiangshouzhengdao.ui.auth.AuthActivity
import com.example.xiangshouzhengdao.ui.history.HistoryActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var outputImage: File
    private lateinit var imageUri: Uri

    private var pendingGalleryBitmap: Bitmap? = null
    private val galleryWritePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val bitmap = pendingGalleryBitmap
        pendingGalleryBitmap = null
        if (granted && bitmap != null) saveImageToGallery(bitmap)
        else if (!granted) Toast.makeText(requireContext(), getString(R.string.storage_permission_toast), Toast.LENGTH_SHORT).show()
    }


    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), getString(R.string.cam_permission_toast), Toast.LENGTH_SHORT).show()
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val bitmap = BitmapFactory.decodeStream(
                    requireContext().contentResolver.openInputStream(imageUri)
                )
                binding.ivAvatar.setImageBitmap(rotateIfRequired(bitmap))
                saveAvatarPath(outputImage.absolutePath)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.photo_fail) + ": " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // 将相册图片复制到应用私有目录，避免URI权限过期
                    requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        bitmap?.let {
                            binding.ivAvatar.setImageBitmap(it)
                            val savedPath = saveBitmapToLocal(it)
                            if (savedPath != null) {
                                saveAvatarPath(savedPath)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), getString(R.string.img_load_fail) + ": " + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreCameraState(savedInstanceState)
        initViews()
        refreshUserStatus()
        loadAvatar()
    }

    // 相机位于另一个Activity，后台回收/旋转后返回仍需找到同一个输出文件。
    private fun restoreCameraState(state: Bundle?) {
        val path = state?.getString("pending_camera_path") ?: return
        outputImage = File(path)
        imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", outputImage)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::outputImage.isInitialized) outState.putString("pending_camera_path", outputImage.absolutePath)
        super.onSaveInstanceState(outState)
    }

    private fun initViews() {
        binding.ivAvatar.setOnClickListener {
            showBottomDialog()
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnMyPrescriptions.setOnClickListener {
            startActivity(Intent(requireContext(), MyPrescriptionsActivity::class.java))
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }

        binding.cardUserInfo.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val repository = DataRepository.getInstance(requireContext())
                if (repository.getCurrentUser() == null) {
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                }
            }
        }
    }

    private fun showBottomDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(com.example.xiangshouzhengdao.R.layout.choosingavatarmenu, FrameLayout(requireContext()), false)
        dialog.setContentView(view)

        view.findViewById<View>(com.example.xiangshouzhengdao.R.id.tv_check_avatar).setOnClickListener {
            dialog.dismiss()
            showBigAvatarDialog()
        }

        view.findViewById<View>(com.example.xiangshouzhengdao.R.id.tv_take_photo).setOnClickListener {
            dialog.dismiss()
            checkCameraPermissionAndOpen()
        }

        view.findViewById<View>(com.example.xiangshouzhengdao.R.id.tv_choose_gallery).setOnClickListener {
            dialog.dismiss()
            checkStoragePermissionAndOpen()
        }

        view.findViewById<View>(com.example.xiangshouzhengdao.R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.cam_permission_title))
                    .setMessage(getString(R.string.cam_permission_msg))
                    .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermissionAndOpen() {
        // 系统文档选择器授予所选文件访问权，不需要读取整个相册的权限。
        openGallery()
    }

    private fun openCamera() {
        try {
            // 使用应用私有目录保存头像，避免被系统清理
            val avatarDir = File(requireContext().filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()

            outputImage = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
            if (outputImage.exists()) outputImage.delete()
            outputImage.createNewFile()

            imageUri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", outputImage
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            takePhotoLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.camera_open_fail) + ": " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickPhotoLauncher.launch(intent)
    }

    private fun showBigAvatarDialog() {
        val bigDialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(com.example.xiangshouzhengdao.R.layout.dialog_view_avatar, FrameLayout(requireContext()), false)
        val imageView = view.findViewById<ImageView>(com.example.xiangshouzhengdao.R.id.iv_big_avatar)
        val saveBtn = view.findViewById<com.google.android.material.button.MaterialButton>(
            com.example.xiangshouzhengdao.R.id.btn_save_to_gallery
        )

        binding.ivAvatar.drawable?.let { drawable ->
            imageView.setImageDrawable(drawable)
        } ?: run {
            imageView.setImageResource(com.example.xiangshouzhengdao.R.drawable.bg_circle)
        }

        saveBtn?.setOnClickListener {
            val drawable = imageView.drawable ?: return@setOnClickListener
            val bitmap = createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            saveImageToGallery(bitmap)
        }

        view.setOnClickListener { bigDialog.dismiss() }

        bigDialog.setContentView(view)
        bigDialog.show()
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        // Android 8/9写公共相册仍需运行时权限；Android 10起使用分区存储。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingGalleryBitmap = bitmap
            galleryWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Avatar_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Xiangshouzhengdao")
                }
            }
            val resolver = requireContext().contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Cannot create gallery entry")
            try {
                resolver.openOutputStream(uri)?.use { out ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out))
                } ?: error("Cannot open gallery entry")
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            Toast.makeText(requireContext(), getString(R.string.avatar_saved), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.save_fail), Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(outputImage.path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
        bitmap.recycle()
        return rotatedBitmap
    }

    private fun saveBitmapToLocal(bitmap: Bitmap): String? {
        return try {
            val avatarDir = File(requireContext().filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()

            val file = File(avatarDir, "avatar_gallery_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveAvatarPath(path: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = DataRepository.getInstance(requireContext())
            val currentUser = repository.getCurrentUser()
            val userId = currentUser?.userId ?: "guest"

            val prefs = requireContext().getSharedPreferences("avatar_prefs_$userId", Activity.MODE_PRIVATE)
            prefs.edit().apply {
                putString("avatar_path", path)
                apply()
            }
        }
    }

    private fun loadAvatar() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = DataRepository.getInstance(requireContext())
            val currentUser = repository.getCurrentUser()
            val userId = currentUser?.userId ?: "guest"

            val prefs = requireContext().getSharedPreferences("avatar_prefs_$userId", Activity.MODE_PRIVATE)
            val path = prefs.getString("avatar_path", null)

            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(path)
                    bitmap?.let { binding.ivAvatar.setImageBitmap(it) }
                }
            }
        }
    }

    private fun refreshUserStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repository = DataRepository.getInstance(requireContext())
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                binding.tvUserName.text = currentUser.nickname
                binding.tvUserStatus.text = getString(R.string.profile_logged_in)
                binding.btnLogout.visibility = View.VISIBLE
            } else {
                binding.tvUserName.text = getString(R.string.profile_login)
                binding.tvUserStatus.text = getString(R.string.profile_not_logged_in)
                binding.btnLogout.visibility = View.GONE
            }
        }
    }

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_msg))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val repository = DataRepository.getInstance(requireContext())
                    val currentUser = repository.getCurrentUser()
                    val userId = currentUser?.userId ?: "guest"

                    repository.logout()
                    // 退出只结束会话，头像仍属于该用户，下次登录继续使用。
                    Toast.makeText(requireContext(), getString(R.string.logout_done), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                    requireActivity().finish()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        refreshUserStatus()
        loadAvatar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}